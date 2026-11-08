package com.businesspilot.module.team.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TeamInviteRequest(
        @NotBlank @Email String email,
        @Pattern(regexp = "ADMIN|MEMBER|VIEWER") String role
) {}
