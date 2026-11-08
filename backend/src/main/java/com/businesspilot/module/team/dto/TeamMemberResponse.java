package com.businesspilot.module.team.dto;

import java.time.Instant;
import java.util.UUID;

public record TeamMemberResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String role,
        Instant joinedAt
) {}
