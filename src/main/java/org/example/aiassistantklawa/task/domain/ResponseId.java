package org.example.aiassistantklawa.task.domain;

import org.springframework.util.Assert;

import java.util.UUID;

public record ResponseId(UUID id) {
    public ResponseId {
        Assert.notNull(id, "must be non empty");
    }
    public ResponseId() {
        this(UUID.randomUUID());
    }
}
