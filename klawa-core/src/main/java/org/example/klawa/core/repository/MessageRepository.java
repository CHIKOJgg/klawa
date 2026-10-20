package org.example.klawa.core.repository;

import org.example.klawa.core.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByConversationIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID conversationId);
}
