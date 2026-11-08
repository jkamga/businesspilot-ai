package com.businesspilot.module.prospects.dto;

import com.businesspilot.module.prospects.domain.Interaction;
import com.businesspilot.module.prospects.domain.InteractionType;

import java.time.Instant;
import java.util.UUID;

public record InteractionResponse(
        UUID id,
        UUID prospectId,
        InteractionType type,
        String content,
        Instant createdAt
) {
    public static InteractionResponse from(Interaction i) {
        return new InteractionResponse(i.getId(), i.getProspectId(), i.getType(), i.getContent(), i.getCreatedAt());
    }
}
