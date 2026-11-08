package com.businesspilot.module.knowledge.dto;

import com.businesspilot.module.knowledge.domain.EntryStatus;
import com.businesspilot.module.knowledge.domain.EntryType;
import com.businesspilot.module.knowledge.domain.KnowledgeEntry;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeEntryResponse(
    UUID id,
    String title,
    String content,
    EntryType type,
    EntryStatus status,
    int wordCount,
    Instant createdAt,
    Instant updatedAt
) {
    public static KnowledgeEntryResponse from(KnowledgeEntry e) {
        return new KnowledgeEntryResponse(
            e.getId(), e.getTitle(), e.getContent(),
            e.getType(), e.getStatus(), e.getWordCount(),
            e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
