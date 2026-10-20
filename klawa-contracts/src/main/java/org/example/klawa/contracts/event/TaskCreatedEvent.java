package org.example.klawa.contracts.event;

import org.example.klawa.contracts.enums.TaskPriority;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Доменное событие создания новой задачи.
 * Публикуется klawa-core в RabbitMQ (topic: klawa.events, routing key: task.created).
 * Обрабатывается klawa-ai-agent для векторной индексации в pgvector.
 */
public record TaskCreatedEvent(
    UUID eventId,
    Long taskId,
    UUID userId,
    Long projectId,
    String title,
    String description,
    TaskPriority priority,
    Instant dueDate,
    Instant occurredAt
) implements Serializable {}
