package com.businesspilot.module.subscriptions.dto;

import com.businesspilot.module.subscriptions.domain.Plan;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        String name,
        String slug,
        BigDecimal priceMonthly,
        BigDecimal priceYearly,
        int maxProspects,
        int maxConversations,
        int maxKnowledgeEntries,
        int maxUsers,
        List<String> features,
        int sortOrder
) {
    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getSlug(),
                plan.getPriceMonthly(),
                plan.getPriceYearly(),
                plan.getMaxProspects(),
                plan.getMaxConversations(),
                plan.getMaxKnowledgeEntries(),
                plan.getMaxUsers(),
                plan.getFeatures(),
                plan.getSortOrder()
        );
    }
}
