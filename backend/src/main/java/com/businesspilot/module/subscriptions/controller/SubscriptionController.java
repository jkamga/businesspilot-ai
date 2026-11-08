package com.businesspilot.module.subscriptions.controller;

import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.subscriptions.dto.CheckoutSessionResponse;
import com.businesspilot.module.subscriptions.dto.PlanResponse;
import com.businesspilot.module.subscriptions.dto.SubscriptionResponse;
import com.businesspilot.module.subscriptions.dto.UpgradeRequest;
import com.businesspilot.module.subscriptions.service.StripeService;
import com.businesspilot.module.subscriptions.service.SubscriptionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "Bearer")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final StripeService stripeService;

    public SubscriptionController(SubscriptionService subscriptionService, StripeService stripeService) {
        this.subscriptionService = subscriptionService;
        this.stripeService = stripeService;
    }

    @GetMapping("/plans")
    public List<PlanResponse> listPlans() {
        return subscriptionService.listActivePlans();
    }

    @GetMapping("/subscriptions/my")
    public SubscriptionResponse getMySubscription() {
        return subscriptionService.getMySubscription();
    }

    @PostMapping("/subscriptions/upgrade")
    public ResponseEntity<SubscriptionResponse> upgrade(@Valid @RequestBody UpgradeRequest request) {
        return ResponseEntity.ok(subscriptionService.upgrade(request));
    }

    @PostMapping("/subscriptions/checkout")
    public ResponseEntity<CheckoutSessionResponse> checkout(@Valid @RequestBody UpgradeRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        String url = stripeService.createCheckoutSession(orgId, request.planSlug(), request.billingCycle());
        return ResponseEntity.ok(new CheckoutSessionResponse(url));
    }
}
