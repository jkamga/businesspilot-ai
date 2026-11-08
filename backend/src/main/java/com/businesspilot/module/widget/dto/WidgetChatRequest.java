package com.businesspilot.module.widget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record WidgetChatRequest(
    UUID conversationId,
    @NotBlank @Size(max = 4000) String message,
    String visitorName
) {}
