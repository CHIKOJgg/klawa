package org.example.aiassistantklawa.agent.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.aiassistantklawa.task.domain.Task;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Agent {

    @Id
    @GeneratedValue
    private Long id;
    private String model_name;

    @ManyToMany
    List<Task> tasks;

}
