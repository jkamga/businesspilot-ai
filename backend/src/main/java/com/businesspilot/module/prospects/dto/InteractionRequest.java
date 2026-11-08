package com.businesspilot.module.prospects.dto;

import com.businesspilot.module.prospects.domain.InteractionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InteractionRequest(
        @NotNull InteractionType type,
        @NotBlank String content
) {}
