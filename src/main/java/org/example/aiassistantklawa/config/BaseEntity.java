package org.example.aiassistantklawa.config;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import org.hibernate.type.descriptor.jdbc.TimestampWithTimeZoneJdbcType;

import java.security.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@MappedSuperclass
public class BaseEntity {
    @Column(name = "created_at", unique = true)
    private Instant  createdAt;
    @Column(name = "updated_at", unique = true)
    private Instant  updatedAt;
    @Column(name = "deleted_at", unique = true)
    private Instant deletedAt;
}
