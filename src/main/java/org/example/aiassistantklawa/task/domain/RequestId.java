package org.example.aiassistantklawa.task.domain;

import org.springframework.util.Assert;

import java.util.UUID;

public record RequestId(UUID id) {
    public RequestId{
        Assert.notNull(id, "must be non empty");
    }
    public RequestId() {
        this(UUID.randomUUID());
    }
}
