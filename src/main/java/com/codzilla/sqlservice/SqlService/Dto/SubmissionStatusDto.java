package com.codzilla.sqlservice.SqlService.Dto;

import com.codzilla.sqlservice.SqlService.model.SqlVerdict;
import com.codzilla.sqlservice.SqlService.model.SubmissionStatus;

public record SubmissionStatusDto(
        Long submissionId,
        SubmissionStatus status,
        SqlVerdict verdict,
        Long executionTimeMs,
        String errorMessage,
        Long kafkaOffset
) {}
