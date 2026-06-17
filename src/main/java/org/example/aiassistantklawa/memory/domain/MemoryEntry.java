package org.example.aiassistantklawa.memory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class MemoryEntry {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    private String key;
    private String value;
    private String source;
}
