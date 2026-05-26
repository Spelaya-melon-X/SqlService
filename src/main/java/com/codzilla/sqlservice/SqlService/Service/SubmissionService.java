package com.codzilla.sqlservice.SqlService.Service;

import com.codzilla.sqlservice.SqlService.DB.*;
import com.codzilla.sqlservice.SqlService.Dto.SubmissionKafkaMessage;
import com.codzilla.sqlservice.SqlService.Dto.SubmissionStatusDto;
import com.codzilla.sqlservice.SqlService.model.SqlVerdict;
import com.codzilla.sqlservice.SqlService.model.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.codzilla.sqlservice.SqlService.kafka.KafkaConfig;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Оркестратор посылок.
 *
 * submit() делает ровно две вещи:
 *   1. Сохраняет посылку в БД со статусом PENDING
 *   2. Отправляет сообщение в Kafka
 *
 * Всё остальное (выполнение SQL, сравнение, вердикт) — в Consumer.
 * Это важно: submit() возвращает управление сразу, не ждёт результата.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;
    private final KafkaTemplate<String, SubmissionKafkaMessage> kafkaTemplate;

    /**
     * Принять посылку от пользователя.
     *
     * @return submissionId — фронт использует его для поллинга статуса
     */
    @Transactional
    public Long submit(Long taskId, UUID userId, String userSqlQuery) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        if (userSqlQuery == null || userSqlQuery.isBlank()) {
            throw new IllegalArgumentException("SQL query cannot be empty");
        }


        SqlSubmission submission = SqlSubmission.builder()
                .task(task)
                .userId(userId)
                .userSqlQuery(userSqlQuery)
                .status(SubmissionStatus.PENDING)
                .verdict(SqlVerdict.SYSTEM_ERROR)
                .build();

        SqlSubmission saved = submissionRepository.save(submission);
        Long submissionId = saved.getSubmissionId();

        log.info("Submission {} saved for task {} by user {}", submissionId, taskId, userId);


        SubmissionKafkaMessage message = new SubmissionKafkaMessage(
                submissionId, taskId, userId, userSqlQuery
        );

        CompletableFuture<SendResult<String, SubmissionKafkaMessage>> future =
                kafkaTemplate.send(KafkaConfig.SUBMISSION_TOPIC, taskId.toString(), message); // Он позволяет запускать фоновые задачи, не блокируя основной поток, и конструировать из них сложные цепочки, в которых результат одной операции автоматически передается в следующую

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send submission {} to Kafka", submissionId, ex);
                markSystemError(submissionId, "Failed to queue submission: " + ex.getMessage());
            } else {
                log.debug("Submission {} sent to Kafka, partition={}, offset={}",
                        submissionId,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return submissionId;
    }

    /** Получить текущий статус посылки — для поллинга с фронта. */
    @Transactional(readOnly = true)
    public SubmissionStatusDto getStatus(Long submissionId) {
        SqlSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        return new SubmissionStatusDto(
                submission.getSubmissionId(),
                submission.getStatus(),
                submission.getVerdict(),
                submission.getExecutionTimeMs(),
                submission.getErrorMessage(),
                submission.getKafkaOffset()
        );
    }

     /** метод который сам открывает транзакцию */
    private void markSystemError(Long submissionId, String errorMessage) {
        submissionRepository.findById(submissionId).ifPresent(s -> {
            s.setStatus(SubmissionStatus.ERROR);
            s.setVerdict(SqlVerdict.SYSTEM_ERROR);
            s.setErrorMessage(errorMessage);
            submissionRepository.save(s);
        });
    }

}