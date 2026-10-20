package org.example.klawa.contracts.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Доменное событие запроса AI-анализа или суммаризации задачи.
 * Публикуется klawa-core в RabbitMQ (routing key: ai.analysis.requested).
 */
public record AiTaskAnalysisRequestedEvent(
    UUID requestId,
    Long taskId,
    UUID userId,
    String prompt,
    Instant requestedAt
) implements Serializable {}
