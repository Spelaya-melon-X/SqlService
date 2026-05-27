package com.codzilla.sqlservice.SqlService.Service;
import com.codzilla.sqlservice.SqlService.Dto.JudgeResult;
import com.codzilla.sqlservice.SqlService.Dto.SqlExecutionResult;
import com.codzilla.sqlservice.SqlService.model.SqlVerdict;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Выносит вердикт по посылке: сравнивает результат пользователя с правильным.
 *
 * Порядок проверок важен — как в реальных олимпиадных системах:
 * 1. Если SQL не выполнился — сразу соответствующий вердикт (CE / RE / TL)
 * 2. Если выполнился — сравниваем хеши результатов
 * 3. Хеши совпали → AC, иначе → WA
 *
 * Почему хеши, а не прямое сравнение List<Map>?
 * Хеш считается один раз при кешировании правильного результата.
 * При каждой новой посылке — сериализуем пользовательский результат и сравниваем хеши строкой.
 * Это O(1) по сравнению с O(n) при сравнении строк всех строк результата.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionJudgeService {

    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    /**
     * Вынести вердикт.
     * @param userResult   — результат выполнения пользовательского SQL
     * @param correctHash  — SHA-256 хеш правильного результата из кеша
     * @param userRows     — строки результата пользователя (для вычисления хеша)
     */
    public JudgeResult judge(
            SqlExecutionResult userResult,
            String correctHash,
            List<Map<String, Object>> userRows
    ) {

        if (!userResult.success()) {
            log.debug("Submission failed with verdict: {}", userResult.failVerdict());
            return new JudgeResult(userResult.failVerdict(), false, userResult.errorMessage());
        }

        String userHash = cacheService.computeHash(serializeForHash(userRows));
        boolean rowsMatched = correctHash.equals(userHash);

        if (rowsMatched) {
            log.debug("Submission accepted");
            return new JudgeResult(SqlVerdict.ACCEPTED, true, null);
        } else {
            log.debug("Wrong answer: hashes differ");
            return new JudgeResult(SqlVerdict.WRONG_ANSWER, false, null);
        }
    }

    /**
     * Сериализуем результат для хеширования.
     * Важно: результат сортируем по всем значениям чтобы порядок строк не влиял на хеш.
     * Если задача требует определённого ORDER BY — убрать сортировку здесь.
     */



    private String serializeForHash(List<Map<String, Object>> rows) {
        try {
            return objectMapper.writeValueAsString(rows);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize for hash", e);
        }
    }

}