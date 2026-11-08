package com.businesspilot.module.conversations.dto;

import com.businesspilot.module.conversations.domain.Message;
import com.businesspilot.module.conversations.domain.MessageRole;

import java.time.Instant;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID conversationId,
        MessageRole role,
        String content,
        Instant createdAt
) {
    public static MessageResponse from(Message m) {
        return new MessageResponse(m.getId(), m.getConversationId(), m.getRole(), m.getContent(), m.getCreatedAt());
    }
}
