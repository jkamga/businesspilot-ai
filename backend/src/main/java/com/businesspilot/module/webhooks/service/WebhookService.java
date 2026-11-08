package com.businesspilot.module.webhooks.service;

import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.webhooks.domain.WebhookEndpoint;
import com.businesspilot.module.webhooks.dto.WebhookEndpointRequest;
import com.businesspilot.module.webhooks.dto.WebhookEndpointResponse;
import com.businesspilot.module.webhooks.repository.WebhookEndpointRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookEndpointRepository repo;
    private final ObjectMapper objectMapper;

    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    // ---- Settings CRUD ----

    @Transactional(readOnly = true)
    public List<WebhookEndpointResponse> list() {
        String orgId = TenantContext.getCurrentTenant();
        return repo.findAllByOrganizationId(UUID.fromString(orgId))
            .stream().map(WebhookEndpointResponse::from).toList();
    }

    @Transactional
    public WebhookEndpointResponse create(WebhookEndpointRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        WebhookEndpoint endpoint = new WebhookEndpoint();
        endpoint.setOrganizationId(UUID.fromString(orgId));
        endpoint.setUrl(request.url());
        endpoint.setEvents(request.events() != null ? request.events() : List.of());
        endpoint.setActive(request.active() == null || request.active());
        endpoint.setSecret(generateSecret());
        return WebhookEndpointResponse.from(repo.save(endpoint));
    }

    @Transactional
    public WebhookEndpointResponse update(UUID id, WebhookEndpointRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        WebhookEndpoint endpoint = repo.findById(id)
            .filter(e -> e.getOrganizationId().equals(UUID.fromString(orgId)))
            .orElseThrow(() -> new NotFoundException("Webhook introuvable"));
        if (request.url() != null) endpoint.setUrl(request.url());
        if (request.events() != null) endpoint.setEvents(request.events());
        if (request.active() != null) endpoint.setActive(request.active());
        return WebhookEndpointResponse.from(repo.save(endpoint));
    }

    @Transactional
    public void delete(UUID id) {
        String orgId = TenantContext.getCurrentTenant();
        WebhookEndpoint endpoint = repo.findById(id)
            .filter(e -> e.getOrganizationId().equals(UUID.fromString(orgId)))
            .orElseThrow(() -> new NotFoundException("Webhook introuvable"));
        repo.delete(endpoint);
    }

    // ---- Event dispatch ----

    @Async
    public void dispatch(String orgId, String eventType, Object data) {
        List<WebhookEndpoint> endpoints = repo.findAllByOrganizationIdAndActiveTrue(UUID.fromString(orgId));
        if (endpoints.isEmpty()) return;

        Map<String, Object> payload = Map.of(
            "event", eventType,
            "timestamp", Instant.now().toString(),
            "data", data
        );

        try {
            String body = objectMapper.writeValueAsString(payload);
            for (WebhookEndpoint endpoint : endpoints) {
                if (!endpoint.getEvents().isEmpty() && !endpoint.getEvents().contains(eventType)) {
                    continue;
                }
                sendWebhook(endpoint, body, eventType);
            }
        } catch (Exception e) {
            log.error("[WEBHOOK] Erreur sérialisation payload org={} event={}: {}", orgId, eventType, e.getMessage());
        }
    }

    private void sendWebhook(WebhookEndpoint endpoint, String body, String eventType) {
        try {
            String signature = hmacSha256(endpoint.getSecret(), body);
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint.getUrl()))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("X-BP-Signature", "sha256=" + signature)
                .header("X-BP-Event", eventType)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<Void> resp = HTTP.send(req, HttpResponse.BodyHandlers.discarding());
            if (resp.statusCode() >= 400) {
                log.warn("[WEBHOOK] Réponse {} pour org={} url={}", resp.statusCode(),
                    endpoint.getOrganizationId(), endpoint.getUrl());
            }
        } catch (Exception e) {
            log.warn("[WEBHOOK] Échec livraison pour org={} url={}: {}",
                endpoint.getOrganizationId(), endpoint.getUrl(), e.getMessage());
        }
    }

    private static String hmacSha256(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private static String generateSecret() {
        return HexFormat.of().formatHex(java.security.SecureRandom.getInstanceStrong().generateSeed(32));
    }
}
