package org.example.aiassistantklawa.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import org.example.aiassistantklawa.config.BaseEntity;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Data
@Entity

public class Notification extends BaseEntity {
    @Id
    private Long id;
    private Long UserId;
    private NotificationType notificationsType;
    private String message;
    private PlatformType platformType;

    @ColumnDefault("false")
    @Column(name = "is_read", nullable = false)
    private Boolean isRead;

    @Column(name = "read_at")
    private OffsetDateTime readAt;


}
