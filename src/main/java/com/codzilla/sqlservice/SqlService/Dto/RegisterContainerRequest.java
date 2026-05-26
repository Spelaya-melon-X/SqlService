package com.codzilla.sqlservice.SqlService.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegisterContainerRequest(
        @NotNull  Long   databaseId,
        @NotBlank String containerId,
        @NotBlank String host,
        @Positive int    port
) {}