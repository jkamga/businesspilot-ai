package com.businesspilot.module.admin.dto;

import java.time.Instant;
import java.util.UUID;

public record AdminOrgResponse(
        UUID id,
        String name,
        String slug,
        String email,
        String countryCode,
        boolean active,
        Instant createdAt,
        String planName,
        String planSlug,
        String subscriptionStatus
) {}
