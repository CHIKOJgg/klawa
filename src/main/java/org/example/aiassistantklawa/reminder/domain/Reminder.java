package org.example.aiassistantklawa.reminder.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.aiassistantklawa.config.BaseEntity;
import org.example.aiassistantklawa.task.domain.TaskStatus;

import java.time.Instant;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Reminder extends BaseEntity {
    @Id
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "task_id")
    private Long taskId;
    @Column(name = "trigger_time")
    private Instant triggerTime;
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    private String message;
    @Column(name = "last_notified_at")
    private Instant lastNotifiedAt;


}
