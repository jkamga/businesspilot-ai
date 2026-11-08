package com.businesspilot.module.subscriptions.controller;

import com.businesspilot.module.subscriptions.dto.PlanResponse;
import com.businesspilot.module.subscriptions.dto.SubscriptionResponse;
import com.businesspilot.module.subscriptions.dto.UpgradeRequest;
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

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
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
}
