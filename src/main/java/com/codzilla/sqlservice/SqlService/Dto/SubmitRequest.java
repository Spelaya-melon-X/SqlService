package com.codzilla.sqlservice.SqlService.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitRequest(
        @NotNull Long taskId,
        @NotNull UUID userId,
        @NotBlank String userSqlQuery
) {}