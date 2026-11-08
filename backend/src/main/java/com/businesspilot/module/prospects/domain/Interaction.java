package com.businesspilot.module.prospects.domain;

import com.businesspilot.common.entity.UuidStringConverter;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interactions")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "prospect_id", nullable = false)
    private UUID prospectId;

    @Convert(converter = UuidStringConverter.class)
    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InteractionType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }

    public UUID getProspectId() { return prospectId; }
    public void setProspectId(UUID prospectId) { this.prospectId = prospectId; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public InteractionType getType() { return type; }
    public void setType(InteractionType type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getCreatedAt() { return createdAt; }
}
