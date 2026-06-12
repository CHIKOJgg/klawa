package org.example.aiassistantklawa.task.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.aiassistantklawa.config.BaseEntity;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Project extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", unique = true)
    private String name;
    @Column(name = "description", unique = true)
    private String description;
    @Column(name = "user_id", unique = true)
    private Long userId;
}
