package com.businesspilot.module.conversations.dto;

import com.businesspilot.module.conversations.domain.Conversation;
import com.businesspilot.module.conversations.domain.ConversationStatus;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String prospectName,
        String prospectEmail,
        ConversationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ConversationResponse from(Conversation c) {
        return new ConversationResponse(c.getId(), c.getProspectName(), c.getProspectEmail(),
                c.getStatus(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
