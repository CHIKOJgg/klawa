package org.example.klawa.contracts.event;

import org.example.klawa.contracts.enums.TaskStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Доменное событие изменения статуса задачи.
 * Публикуется klawa-core в RabbitMQ (routing key: task.status-changed).
 */
public record TaskStatusUpdatedEvent(
    UUID eventId,
    Long taskId,
    UUID userId,
    TaskStatus oldStatus,
    TaskStatus newStatus,
    Instant occurredAt
) implements Serializable {}
