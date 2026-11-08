package com.businesspilot.module.webhooks.controller;

import com.businesspilot.module.webhooks.dto.WhatsAppInboundPayload;
import com.businesspilot.module.webhooks.service.WhatsAppWebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    @Value("${app.whatsapp.verify-token:}")
    private String verifyToken;

    private final WhatsAppWebhookService webhookService;
    private final ObjectMapper objectMapper;

    public WhatsAppWebhookController(WhatsAppWebhookService webhookService, ObjectMapper objectMapper) {
        this.webhookService = webhookService;
        this.objectMapper = objectMapper;
    }

    /** Vérification du webhook par Meta (GET) */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("Webhook WhatsApp vérifié");
            return ResponseEntity.ok(challenge);
        }
        log.warn("Vérification webhook échouée — token invalide");
        return ResponseEntity.status(403).body("Forbidden");
    }

    /** Réception des événements Meta (POST) */
    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String rawBody) {
        if (!webhookService.verifySignature(rawBody, signature)) {
            log.warn("Signature HMAC invalide — webhook rejeté");
            return ResponseEntity.status(403).build();
        }
        try {
            WhatsAppInboundPayload payload = objectMapper.readValue(rawBody, WhatsAppInboundPayload.class);
            webhookService.processAsync(payload);
        } catch (Exception e) {
            log.error("Erreur parsing payload WhatsApp : {}", e.getMessage());
        }
        // Toujours 200 pour éviter les retries Meta
        return ResponseEntity.ok().build();
    }
}
