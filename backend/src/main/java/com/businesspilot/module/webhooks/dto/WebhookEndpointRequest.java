package com.businesspilot.module.webhooks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record WebhookEndpointRequest(
        @NotBlank @URL @Size(max = 2048) String url,
        List<String> events,
        Boolean active
) {}
