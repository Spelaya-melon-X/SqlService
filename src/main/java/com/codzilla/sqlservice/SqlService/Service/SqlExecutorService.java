package com.codzilla.sqlservice.SqlService.Service;


import com.codzilla.sqlservice.SqlService.DB.DockerContainers;
import com.codzilla.sqlservice.SqlService.Dto.SqlExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**Выполняет SQL-запросы над тестовыми контейнерами.*/
@Slf4j
@Service
@RequiredArgsConstructor
public class SqlExecutorService {

    private final ContainerDataSourceService dataSourceService;

    /**
     * Выполнить SQL над конкретным контейнером.
     *
     * @param sql         — запрос пользователя
     * @param container   — контейнер из docker_containers
     * @param timeLimitMs — лимит времени задачи (из tasks.time_limit_ms)
     * @return результат с временем выполнения и списком строк
     */
    public SqlExecutionResult execute(String sql, DockerContainers container, int timeLimitMs) {
        JdbcTemplate jdbcTemplate = dataSourceService.getJdbcTemplate(container);

        long startTime = System.currentTimeMillis();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql); /* result after sql */
            long elapsed = System.currentTimeMillis() - startTime;

            log.debug("SQL executed in {}ms, {} rows returned", elapsed, rows.size());

            if (elapsed > timeLimitMs) {
                return SqlExecutionResult.timeLimitExceeded(elapsed);
            }

            return SqlExecutionResult.success(rows, elapsed);

        } catch (DataAccessException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("SQL execution error: {}", e.getMessage());

            if (e instanceof BadSqlGrammarException) {
                return SqlExecutionResult.compilationError(e.getMessage(), elapsed);
            }
            return SqlExecutionResult.runtimeError(e.getMessage(), elapsed);
        }
    }
}