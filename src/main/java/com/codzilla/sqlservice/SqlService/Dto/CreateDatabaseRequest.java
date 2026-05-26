package com.codzilla.sqlservice.SqlService.Dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDatabaseRequest(
        @NotBlank String name,
        @NotBlank String schemaName
) {}