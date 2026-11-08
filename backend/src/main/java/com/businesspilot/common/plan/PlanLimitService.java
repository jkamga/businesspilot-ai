package com.businesspilot.common.plan;

import com.businesspilot.common.exception.PlanLimitExceededException;
import com.businesspilot.module.subscriptions.domain.Plan;
import com.businesspilot.module.subscriptions.repository.PlanRepository;
import com.businesspilot.module.subscriptions.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlanLimitService {

    private final SubscriptionRepository subscriptionRepo;
    private final PlanRepository planRepo;

    public void checkLimit(String orgId, PlanResource resource, long currentCount) {
        subscriptionRepo.findByOrganizationId(orgId).ifPresent(sub ->
            planRepo.findById(sub.getPlanId()).ifPresent(plan -> {
                int limit = limitFor(plan, resource);
                if (limit != -1 && currentCount >= limit) {
                    throw new PlanLimitExceededException(resource.getLabel(), limit);
                }
            })
        );
    }

    private int limitFor(Plan plan, PlanResource resource) {
        return switch (resource) {
            case PROSPECT -> plan.getMaxProspects();
            case CONVERSATION -> plan.getMaxConversations();
            case KNOWLEDGE_ENTRY -> plan.getMaxKnowledgeEntries();
        };
    }
}
