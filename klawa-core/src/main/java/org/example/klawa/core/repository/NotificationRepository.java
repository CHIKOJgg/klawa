package org.example.klawa.core.repository;

import org.example.klawa.core.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);

    List<Notification> findAllByUserIdAndIsReadFalseAndDeletedAtIsNull(UUID userId);
}
