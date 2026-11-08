package com.businesspilot.module.conversations.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ConversationRequest(
        @Size(max = 255) String prospectName,
        @Email @Size(max = 255) String prospectEmail
) {}
