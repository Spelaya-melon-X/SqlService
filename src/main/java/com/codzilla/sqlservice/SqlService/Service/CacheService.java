package com.codzilla.sqlservice.SqlService.Service;

import com.codzilla.sqlservice.SqlService.DB.DockerContainers;
import com.codzilla.sqlservice.SqlService.DB.Task;
import com.codzilla.sqlservice.SqlService.DB.TaskResultCache;
import com.codzilla.sqlservice.SqlService.DB.TaskResultCacheRepository;
import com.codzilla.sqlservice.SqlService.model.CachedResult;
import com.codzilla.sqlservice.SqlService.model.SqlExecutionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Кешируем результат выполнения правильного SQL-запроса задачи.
 *
 * Логика проста:
 * 1. Пришла посылка → смотрим есть ли кеш для этой задачи
 * 2. Есть → берём resultJson из кеша, не выполняем correct_sql заново
 * 3. Нет → выполняем correct_sql, сохраняем в кеш, возвращаем результат
 *
 * Зачем result_hash? Для быстрого сравнения: сравниваем хеши строкой,
 * а не десериализуем и сравниваем List<Map<>> каждый раз.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final TaskResultCacheRepository cacheRepository;
    private final SqlExecutorService sqlExecutor;
    private final ObjectMapper objectMapper;

    /**
     * Получить закешированный результат для задачи.
     * Если кеша нет — выполняет correct_sql, сохраняет и возвращает.
     *
     * @param task      — задача (нужны correct_sql, time_limit_ms)
     * @param container — контейнер с развёрнутой тестовой БД
     * @return CachedResult с resultJson и resultHash
     */
    @Transactional
    public CachedResult getOrComputeCache(Task task, DockerContainers container) {
        /* available in the cache*/
        Optional<TaskResultCache> existing = cacheRepository.findByTask(task);
        if (existing.isPresent()) {
            log.debug("Cache hit for task {}", task.getTaskId());
            TaskResultCache cache = existing.get();
            return new CachedResult(deserialize(cache.getResultJson()), cache.getResultHash());
        }

        /* no cache */
        log.info("Cache miss for task {}, executing correct SQL", task.getTaskId());
        SqlExecutionResult executionResult = sqlExecutor.execute(
                task.getCorrectSqlResponse(),
                container,
                task.getTimeLimitMs() != null ? task.getTimeLimitMs() : 30_000
        );

        if (!executionResult.success()) {
            throw new IllegalStateException(
                    "Correct SQL for task " + task.getTaskId() + " failed: " + executionResult.errorMessage()
            );
        }

        String resultJson = serialize(executionResult.rows());
        String resultHash = computeHash(resultJson);

        // Сохраняем в кеш
        TaskResultCache newCache = TaskResultCache.builder()
                .task(task)
                .resultJson(resultJson)
                .resultHash(resultHash)
                .build();
        cacheRepository.save(newCache);
        log.info("Saved cache for task {}, hash={}", task.getTaskId(), resultHash);

        return new CachedResult(executionResult.rows(), resultHash);
    }

    /**
     * Посчитать SHA-256 хеш строки.
     * Используется чтобы быстро сравнивать результаты без полной десериализации.
     */
    public String computeHash(String json) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(json.getBytes());
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String serialize(List<Map<String, Object>> rows) {
        try {
            return objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize query result", e);
        }
    }

    private List<Map<String, Object>> deserialize(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize cached result", e);
        }
    }


}