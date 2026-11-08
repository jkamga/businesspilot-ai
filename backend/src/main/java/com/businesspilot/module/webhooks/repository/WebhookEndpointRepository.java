package com.businesspilot.module.webhooks.repository;

import com.businesspilot.module.webhooks.domain.WebhookEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {
    List<WebhookEndpoint> findAllByOrganizationIdAndActiveTrue(UUID orgId);
    List<WebhookEndpoint> findAllByOrganizationId(UUID orgId);
}
