package org.example.aiassistantklawa.reminder.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.aiassistantklawa.task.domain.Task;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_event")
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue
    private Long id;
    //private String name;
    private String title;
    private String body;
    @OneToOne
    @JoinColumn(name = "task_id")
    private Task task;

}
