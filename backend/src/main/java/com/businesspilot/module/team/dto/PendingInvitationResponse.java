package com.businesspilot.module.team.dto;

import java.time.Instant;
import java.util.UUID;

public record PendingInvitationResponse(
        UUID id,
        String email,
        String role,
        Instant expiresAt,
        Instant createdAt
) {}
