package com.businesspilot.module.auth.dto;

import java.util.UUID;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    UUID userId,
    String email,
    String firstName,
    String lastName,
    UUID organizationId,
    String role,
    boolean onboardingRequired
) {}
