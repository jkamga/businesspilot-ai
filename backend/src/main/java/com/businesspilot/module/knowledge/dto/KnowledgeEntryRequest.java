package com.businesspilot.module.knowledge.dto;

import com.businesspilot.module.knowledge.domain.EntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record KnowledgeEntryRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank String content,
    @NotNull EntryType type
) {}
