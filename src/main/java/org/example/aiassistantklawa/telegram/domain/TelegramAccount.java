package org.example.aiassistantklawa.telegram.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class TelegramAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "chat_id")
    private Long chatId;
    @Column(name = "telegram_username")
    private String telegramUsername;
    @Column(name = "linked_at")
    private Instant linkedAt;
}
