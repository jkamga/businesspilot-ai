package com.businesspilot.module.conversations.repository;

import com.businesspilot.module.conversations.domain.Conversation;
import com.businesspilot.module.conversations.domain.ConversationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Page<Conversation> findAllByOrganizationId(String organizationId, Pageable pageable);

    Page<Conversation> findAllByOrganizationIdAndStatus(String organizationId, ConversationStatus status, Pageable pageable);

    Optional<Conversation> findByIdAndOrganizationId(UUID id, String organizationId);
}
