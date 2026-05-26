package com.codzilla.sqlservice.SqlService.Dto;


import com.codzilla.sqlservice.SqlService.model.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTaskRequest(
        @NotNull Long databaseId,
        @NotBlank String title,
        @NotNull TaskType type,
        String description,
        @NotBlank String correctSqlQuery,
        @Positive Integer timeLimitMs    // null → дефолт 30 000 мс
) {}