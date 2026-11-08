package com.businesspilot.module.subscriptions.dto;

import com.businesspilot.module.subscriptions.domain.BillingCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpgradeRequest(
        @NotBlank String planSlug,
        @NotNull BillingCycle billingCycle
) {}
