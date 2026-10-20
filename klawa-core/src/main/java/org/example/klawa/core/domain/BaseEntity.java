package org.example.klawa.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Базовый класс для всех сущностей с поддержкой Spring Data JPA Auditing и Soft Delete.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "deleted_at")
    private Instant deletedAt;

    /**
     * Проверка, удалена ли сущность (Soft Delete).
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Пометить сущность как удаленную.
     */
    public void markAsDeleted() {
        this.deletedAt = Instant.now();
    }
}
