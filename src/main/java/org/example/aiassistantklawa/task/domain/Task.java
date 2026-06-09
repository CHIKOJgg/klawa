package org.example.aiassistantklawa.task.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aiassistantklawa.agent.domain.Agent;
import org.example.aiassistantklawa.reminder.domain.Event;
import org.example.aiassistantklawa.user.domain.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.OPEN;
    @Column(name = "due_date")
    private LocalDateTime created;
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "name_id", nullable = false)
    //private User user;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "agents_tasks",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "agent_id")
    )
    private List<Agent> agents;

    @OneToOne
    @JoinColumn(name = "event_id")
    private Event event;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}
