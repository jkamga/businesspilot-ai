package com.businesspilot.module.subscriptions.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String slug;

    @Column(name = "price_monthly", precision = 10, scale = 2)
    private BigDecimal priceMonthly;

    @Column(name = "price_yearly", precision = 10, scale = 2)
    private BigDecimal priceYearly;

    @Column(name = "max_prospects", nullable = false)
    private int maxProspects = -1;

    @Column(name = "max_conversations", nullable = false)
    private int maxConversations = -1;

    @Column(name = "max_knowledge_entries", nullable = false)
    private int maxKnowledgeEntries = -1;

    @Column(name = "max_users", nullable = false)
    private int maxUsers = -1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> features;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public BigDecimal getPriceMonthly() { return priceMonthly; }
    public BigDecimal getPriceYearly() { return priceYearly; }
    public int getMaxProspects() { return maxProspects; }
    public int getMaxConversations() { return maxConversations; }
    public int getMaxKnowledgeEntries() { return maxKnowledgeEntries; }
    public int getMaxUsers() { return maxUsers; }
    public List<String> getFeatures() { return features; }
    public boolean isActive() { return active; }
    public int getSortOrder() { return sortOrder; }
}
