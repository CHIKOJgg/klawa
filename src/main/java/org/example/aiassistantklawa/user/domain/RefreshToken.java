package org.example.aiassistantklawa.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data

public class RefreshToken {
    @Id
    private UUID uuid;
    private String token;
    private long userId;
    private Instant expiresAt;
    private Boolean revoked;
    private Instant createdAt;
}
