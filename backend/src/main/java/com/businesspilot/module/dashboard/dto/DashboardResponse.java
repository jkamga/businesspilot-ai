package com.businesspilot.module.dashboard.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DashboardResponse(
        ProspectStats prospects,
        ConversationStats conversations,
        KnowledgeStats knowledge,
        List<RecentConversation> recentConversations,
        List<RecentProspect> recentProspects
) {
    public record ProspectStats(
            long total,
            Map<String, Long> byStatus
    ) {}

    public record ConversationStats(
            long total,
            long open,
            long resolved
    ) {}

    public record KnowledgeStats(
            long total,
            long active,
            long draft
    ) {}

    public record RecentConversation(
            UUID id,
            String prospectName,
            String prospectEmail,
            String status,
            Instant updatedAt
    ) {}

    public record RecentProspect(
            UUID id,
            String firstName,
            String lastName,
            String company,
            String status,
            Instant updatedAt
    ) {}
}
