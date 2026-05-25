package com.codzilla.sqlservice.SqlService.model;
public record JudgeResult(SqlVerdict verdict, boolean rowsMatched, String errorMessage) {}
