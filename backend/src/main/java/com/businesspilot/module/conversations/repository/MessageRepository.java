package com.businesspilot.module.conversations.repository;

import com.businesspilot.module.conversations.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<Message> findAllByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    boolean existsByExternalId(String externalId);
}
