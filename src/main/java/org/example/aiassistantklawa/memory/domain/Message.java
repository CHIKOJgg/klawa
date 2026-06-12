package org.example.aiassistantklawa.memory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.example.aiassistantklawa.user.domain.Role;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class Message {
    @Id
    @GeneratedValue
    private UUID id;
    @Column(name = "convesation_id")
    private Long conversationId;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String content;
    @Column(name = "token_count")
    private Integer tokenCount;

}
