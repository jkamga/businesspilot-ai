package com.businesspilot.module.conversations.dto;

import jakarta.validation.constraints.Pattern;

public record MessageFeedbackRequest(
        @Pattern(regexp = "POSITIVE|NEGATIVE", message = "feedback doit etre POSITIVE ou NEGATIVE")
        String feedback
) {}
