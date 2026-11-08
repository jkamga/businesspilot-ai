package com.businesspilot.module.subscriptions.service;

import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.subscriptions.domain.BillingCycle;
import com.businesspilot.module.subscriptions.domain.Plan;
import com.businesspilot.module.subscriptions.domain.Subscription;
import com.businesspilot.module.subscriptions.domain.SubscriptionStatus;
import com.businesspilot.module.subscriptions.dto.PlanResponse;
import com.businesspilot.module.subscriptions.dto.SubscriptionResponse;
import com.businesspilot.module.subscriptions.dto.UpgradeRequest;
import com.businesspilot.module.subscriptions.repository.PlanRepository;
import com.businesspilot.module.subscriptions.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SubscriptionService {

    private static final String FREE_PLAN_SLUG = "free";

    private final PlanRepository planRepo;
    private final SubscriptionRepository subscriptionRepo;

    public SubscriptionService(PlanRepository planRepo, SubscriptionRepository subscriptionRepo) {
        this.planRepo = planRepo;
        this.subscriptionRepo = subscriptionRepo;
    }

    public List<PlanResponse> listActivePlans() {
        return planRepo.findAllByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(PlanResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionResponse getMySubscription() {
        String orgId = TenantContext.getCurrentTenant();
        Subscription sub = subscriptionRepo.findByOrganizationId(orgId)
                .orElseGet(() -> createFreeSub(orgId));
        Plan plan = planRepo.findById(sub.getPlanId())
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        return SubscriptionResponse.from(sub, PlanResponse.from(plan));
    }

    @Transactional
    public SubscriptionResponse upgrade(UpgradeRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        Plan plan = planRepo.findBySlug(request.planSlug())
                .orElseThrow(() -> new NotFoundException("Plan not found: " + request.planSlug()));

        Subscription sub = subscriptionRepo.findByOrganizationId(orgId)
                .orElseGet(() -> createFreeSub(orgId));

        sub.setPlanId(plan.getId());
        sub.setBillingCycle(request.billingCycle());
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setCurrentPeriodStart(Instant.now());
        sub.setCurrentPeriodEnd(periodEnd(request.billingCycle()));

        subscriptionRepo.save(sub);
        return SubscriptionResponse.from(sub, PlanResponse.from(plan));
    }

    private Subscription createFreeSub(String orgId) {
        Plan freePlan = planRepo.findBySlug(FREE_PLAN_SLUG)
                .orElseThrow(() -> new NotFoundException("Free plan not found"));
        Subscription sub = new Subscription();
        sub.setOrganizationId(orgId);
        sub.setPlanId(freePlan.getId());
        sub.setBillingCycle(BillingCycle.MONTHLY);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setCurrentPeriodStart(Instant.now());
        return subscriptionRepo.save(sub);
    }

    private Instant periodEnd(BillingCycle cycle) {
        return cycle == BillingCycle.YEARLY
                ? Instant.now().plus(365, ChronoUnit.DAYS)
                : Instant.now().plus(30, ChronoUnit.DAYS);
    }
}
