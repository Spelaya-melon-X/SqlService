package com.codzilla.sqlservice.SqlService.Dto;

import com.codzilla.sqlservice.SqlService.model.SqlVerdict;

public record JudgeResult(SqlVerdict verdict, boolean rowsMatched, String errorMessage) {}
