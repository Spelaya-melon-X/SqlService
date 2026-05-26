package com.codzilla.sqlservice.SqlService.Service;


import com.codzilla.sqlservice.SqlService.DB.*;
import com.codzilla.sqlservice.SqlService.Dto.CreateTaskRequest;
import com.codzilla.sqlservice.SqlService.Dto.UpdateTaskRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final DatabasesRepository databasesRepository;
    private final TaskResultCacheRepository cacheRepository;

    @Transactional
    public Task create(CreateTaskRequest req) {
        DatabaseEntity db = databasesRepository.findById(req.databaseId())
                .orElseThrow(() -> new IllegalArgumentException("Database not found: " + req.databaseId()));

        Task task = Task.builder()
                .database(db)
                .title(req.title())
                .type(req.type())
                .description(req.description())
                .correctSqlResponse(req.correctSqlQuery())
                .timeLimitMs(req.timeLimitMs() != null ? req.timeLimitMs() : 30_000)
                .build();

        Task saved = taskRepository.save(task);
        log.info("Created task {} '{}'", saved.getTaskId(), saved.getTitle());
        return saved;
    }

    @Transactional(readOnly = true)
    public Task getById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
    }

    @Transactional(readOnly = true)
    public List<Task> getAll() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> getByDatabase(Long databaseId) {
        DatabaseEntity db = databasesRepository.findById(databaseId)
                .orElseThrow(() -> new IllegalArgumentException("Database not found: " + databaseId));
        return taskRepository.findAllByDatabase(db);
    }

    /**
     * PATCH-обновление: меняем только переданные (не null) поля.
     * При изменении correct_sql — инвалидируем кеш, иначе старый результат останется.
     */
    @Transactional
    public Task update(Long taskId, UpdateTaskRequest req) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        boolean sqlChanged = false;

        if (req.title()          != null) task.setTitle(req.title());
        if (req.type()           != null) task.setType(req.type());
        if (req.description()    != null) task.setDescription(req.description());
        if (req.timeLimitMs()    != null) task.setTimeLimitMs(req.timeLimitMs());
        if (req.correctSqlQuery()!= null) {
            task.setCorrectSqlResponse(req.correctSqlQuery());
            sqlChanged = true;
        }

        Task saved = taskRepository.save(task);

        if (sqlChanged) {
            cacheRepository.findByTask(saved).ifPresent(cache -> {
                cacheRepository.delete(cache);
                log.info("Invalidated cache for task {} after SQL update", taskId);
            });
        }

        log.info("Updated task {}", taskId);
        return saved;
    }

    /**
     * Удалить задачу + каскадно кеш.
     * Посылки не удаляем — нужны для истории.
     */
    @Transactional
    public void delete(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        cacheRepository.findByTask(task).ifPresent(cacheRepository::delete);
        taskRepository.delete(task);
        log.info("Deleted task {}", taskId);
    }
}