package com.businesspilot.module.webhooks.dto;

import com.businesspilot.module.webhooks.domain.WebhookEndpoint;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WebhookEndpointResponse(
        UUID id,
        String url,
        String secret,
        List<String> events,
        boolean active,
        Instant createdAt
) {
    public static WebhookEndpointResponse from(WebhookEndpoint w) {
        return new WebhookEndpointResponse(w.getId(), w.getUrl(), w.getSecret(),
            w.getEvents(), w.isActive(), w.getCreatedAt());
    }
}
