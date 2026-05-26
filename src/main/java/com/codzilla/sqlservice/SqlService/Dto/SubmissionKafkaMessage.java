package com.codzilla.sqlservice.SqlService.Dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO который летит через Kafka.
 * Содержит только примитивы — не тащим JPA-сущности в очередь.
 * record — иммутабельный, сериализуется Jackson-ом.
 */
public record SubmissionKafkaMessage(
        Long submissionId,
        Long taskId,
        UUID userId,
        String userSqlQuery
) implements Serializable {}