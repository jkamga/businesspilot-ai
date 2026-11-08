package com.businesspilot.module.subscriptions.controller;

import com.businesspilot.common.exception.BusinessException;
import com.businesspilot.module.subscriptions.service.StripeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook/stripe")
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeService stripeService;

    public StripeWebhookController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestHeader("Stripe-Signature") String sigHeader,
            @RequestBody String rawBody) {
        try {
            stripeService.handleWebhookEvent(rawBody, sigHeader);
            return ResponseEntity.ok().build();
        } catch (BusinessException e) {
            log.warn("Webhook Stripe rejeté : {}", e.getMessage());
            return ResponseEntity.status(400).build();
        }
    }
}
