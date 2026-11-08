package com.businesspilot.module.knowledge.domain;

import com.businesspilot.common.entity.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "knowledge_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeEntry extends TenantAwareEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "entry_type")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    private EntryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "entry_status")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.NAMED_ENUM)
    private EntryStatus status;

    @Column(name = "word_count", nullable = false)
    private int wordCount;
}
