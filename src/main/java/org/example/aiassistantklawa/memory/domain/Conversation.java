package org.example.aiassistantklawa.memory.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.aiassistantklawa.config.BaseEntity;
import org.springframework.objenesis.instantiator.util.UnsafeUtils;

import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Conversation  extends BaseEntity{
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "user_id")
    private Long userId;
    private String title;

}
