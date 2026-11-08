package com.businesspilot.module.subscriptions.dto;

import com.businesspilot.module.subscriptions.domain.BillingCycle;
import com.businesspilot.module.subscriptions.domain.Subscription;
import com.businesspilot.module.subscriptions.domain.SubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        PlanResponse plan,
        SubscriptionStatus status,
        BillingCycle billingCycle,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        Instant createdAt
) {
    public static SubscriptionResponse from(Subscription sub, PlanResponse plan) {
        return new SubscriptionResponse(
                sub.getId(),
                plan,
                sub.getStatus(),
                sub.getBillingCycle(),
                sub.getCurrentPeriodStart(),
                sub.getCurrentPeriodEnd(),
                sub.getCreatedAt()
        );
    }
}
