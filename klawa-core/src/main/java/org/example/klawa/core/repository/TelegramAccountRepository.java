package org.example.klawa.core.repository;

import org.example.klawa.core.domain.TelegramAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TelegramAccountRepository extends JpaRepository<TelegramAccount, Long> {

    Optional<TelegramAccount> findByUserId(UUID userId);

    Optional<TelegramAccount> findByTelegramChatId(Long telegramChatId);

    boolean existsByTelegramChatId(Long telegramChatId);
}
