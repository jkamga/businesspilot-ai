package com.businesspilot.module.knowledge.dto;

import java.util.List;

public record KnowledgePageResponse(
    List<KnowledgeEntryResponse> content,
    long totalElements,
    int totalPages,
    int page,
    int size
) {}
