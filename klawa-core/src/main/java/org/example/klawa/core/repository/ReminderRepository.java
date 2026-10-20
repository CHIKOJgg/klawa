package org.example.klawa.core.repository;

import org.example.klawa.core.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findAllByUserId(UUID userId);

    List<Reminder> findAllByTriggerTimeLessThanEqualAndStatus(Instant triggerTime, String status);
}
