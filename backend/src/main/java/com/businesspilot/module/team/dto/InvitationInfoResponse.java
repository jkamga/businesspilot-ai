package com.businesspilot.module.team.dto;

import java.util.UUID;

public record InvitationInfoResponse(
        UUID token,
        String orgName,
        String inviterName,
        String role,
        String email
) {}
