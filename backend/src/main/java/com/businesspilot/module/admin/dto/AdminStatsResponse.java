package com.businesspilot.module.admin.dto;

import java.util.Map;

public record AdminStatsResponse(
        long totalOrganizations,
        long activeOrganizations,
        long totalUsers,
        Map<String, Long> subscriptionsByPlan
) {}
