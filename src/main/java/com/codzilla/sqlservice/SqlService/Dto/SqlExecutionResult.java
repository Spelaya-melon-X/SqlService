package com.codzilla.sqlservice.SqlService.Dto;

import com.codzilla.sqlservice.SqlService.model.SqlVerdict;

import java.util.List;
import java.util.Map;

public record SqlExecutionResult(
        boolean success,
        List<Map<String, Object>> rows,
        long executionTimeMs,
        SqlVerdict failVerdict,
        String errorMessage
    ) {
    public static SqlExecutionResult success(List<Map<String, Object>> rows, long time) {
        return new SqlExecutionResult(true, rows, time, null, null);
    }

    public static SqlExecutionResult timeLimitExceeded(long time) {
        return new SqlExecutionResult(false, List.of(), time,
                SqlVerdict.TIME_LIMIT_EXCEEDED, "Превышен лимит времени");
    }

    public static SqlExecutionResult compilationError(String msg, long time) {
        return new SqlExecutionResult(false, List.of(), time,
                SqlVerdict.COMPILATION_ERROR, msg);
    }

    public static SqlExecutionResult runtimeError(String msg, long time) {
        return new SqlExecutionResult(false, List.of(), time,
                SqlVerdict.RUNTIME_ERROR, msg);
    }
}