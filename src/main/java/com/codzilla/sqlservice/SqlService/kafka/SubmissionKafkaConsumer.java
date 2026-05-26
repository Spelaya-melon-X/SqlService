package com.codzilla.sqlservice.SqlService.kafka;
import com.codzilla.sqlservice.SqlService.DB.*;
import com.codzilla.sqlservice.SqlService.Dto.CachedResult;
import com.codzilla.sqlservice.SqlService.Dto.JudgeResult;
import com.codzilla.sqlservice.SqlService.Dto.SqlExecutionResult;
import com.codzilla.sqlservice.SqlService.Dto.SubmissionKafkaMessage;
import com.codzilla.sqlservice.SqlService.Service.CacheService;
import com.codzilla.sqlservice.SqlService.Service.SqlExecutorService;
import com.codzilla.sqlservice.SqlService.Service.SubmissionJudgeService;
import com.codzilla.sqlservice.SqlService.model.ContainerStatus;
import com.codzilla.sqlservice.SqlService.model.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka Consumer — мозг системы оценивания.
 *
 * Порядок обработки каждого сообщения:
 *   1. Читаем kafka_offset из метаданных записи — сохраняем в submission
 *   2. Берём свободный Docker-контейнер для задачи
 *   3. Берём/вычисляем кеш правильного ответа
 *   4. Выполняем SQL пользователя
 *   5. Судья выносит вердикт
 *   6. Обновляем submission в БД
 *   7. Коммитим offset в Kafka (только если всё ок)
 *
 * При любом необработанном исключении — offset НЕ коммитится,
 * Kafka доставит сообщение повторно.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionKafkaConsumer {

    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final DockerContainersRepository containerRepository;
    private final CacheService cacheService;
    private final SqlExecutorService sqlExecutor;
    private final SubmissionJudgeService judgeService;

    @KafkaListener(
            topics = KafkaConfig.SUBMISSION_TOPIC,
            groupId = "sql-judge-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(
            ConsumerRecord<String, SubmissionKafkaMessage> record,
            Acknowledgment ack
    ) {
        SubmissionKafkaMessage message = record.value();
        long kafkaOffset = record.offset(); // Порядковый номер
        int partition = record.partition(); // номер партиции

        log.info("Processing submission {} from partition={} offset={}",
                message.submissionId(), partition, kafkaOffset);


        SqlSubmission submission = submissionRepository.findById(message.submissionId())
                .orElseGet(() -> {
                    log.error("Submission {} not found in DB — skipping", message.submissionId());
                    return null;
                });

        if (submission == null) {
            ack.acknowledge();
            return;
        }

        submission.setKafkaOffset(kafkaOffset);

        try {
            Task task = taskRepository.findById(message.taskId())
                    .orElseThrow(() -> new IllegalStateException("Task not found: " + message.taskId()));

            DockerContainers container = containerRepository
                    .findFirstByDatabaseAndStatus(task.getDatabase(), ContainerStatus.RUNNING)
                    .orElseThrow(() -> new IllegalStateException(
                            "No running containers for database: " + task.getDatabase().getId()
                    ));

            /** get cache correct result*/
            CachedResult correctResult = cacheService.getOrComputeCache(task, container);

            /** execute user sql */
            SqlExecutionResult userResult = sqlExecutor.execute(
                    message.userSqlQuery(),
                    container,
                    task.getTimeLimitMs() != null ? task.getTimeLimitMs() : 30_000
            );

            /** get verdict */
            JudgeResult judgeResult = judgeService.judge(
                    userResult,
                    correctResult.hash(),
                    userResult.rows()
            );

            /** update submit results*/
            submission.setStatus(SubmissionStatus.DONE);
            submission.setVerdict(judgeResult.verdict());
            submission.setRowsMatched(judgeResult.rowsMatched());
            submission.setExecutionTimeMs(userResult.executionTimeMs());
            if (judgeResult.errorMessage() != null) {
                submission.setErrorMessage(judgeResult.errorMessage());
            }

            submissionRepository.save(submission);

            log.info("Submission {} judged: verdict={}, time={}ms",
                    message.submissionId(), judgeResult.verdict(), userResult.executionTimeMs());
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Error processing submission {}: {}", message.submissionId(), e.getMessage(), e);

            submission.setStatus(SubmissionStatus.ERROR);
            submission.setErrorMessage("System error: " + e.getMessage());
            submissionRepository.save(submission);

            /** not commit offset , restart send in kafka */
            throw new RuntimeException("Submission processing failed, will retry", e);
        }
    }
}