package com.businesspilot.module.subscriptions.service;

import com.businesspilot.common.exception.BusinessException;
import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.module.subscriptions.domain.BillingCycle;
import com.businesspilot.module.subscriptions.domain.Plan;
import com.businesspilot.module.subscriptions.repository.PlanRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StripeService {

    private static final Logger log = LoggerFactory.getLogger(StripeService.class);

    @Value("${app.stripe.secret-key:}")
    private String secretKey;

    @Value("${app.stripe.webhook-secret:}")
    private String webhookSecret;

    @Value("${app.stripe.success-url:http://localhost:4200/subscription?success=true}")
    private String successUrl;

    @Value("${app.stripe.cancel-url:http://localhost:4200/subscription?cancelled=true}")
    private String cancelUrl;

    private final PlanRepository planRepo;
    private final SubscriptionService subscriptionService;

    public StripeService(PlanRepository planRepo, SubscriptionService subscriptionService) {
        this.planRepo = planRepo;
        this.subscriptionService = subscriptionService;
    }

    public String createCheckoutSession(String orgId, String planSlug, BillingCycle billingCycle) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new BusinessException("Stripe non configuré");
        }
        Stripe.apiKey = secretKey;

        Plan plan = planRepo.findBySlug(planSlug)
                .orElseThrow(() -> new NotFoundException("Plan introuvable : " + planSlug));

        BigDecimal price = billingCycle == BillingCycle.YEARLY ? plan.getPriceYearly() : plan.getPriceMonthly();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Ce plan n'a pas de tarif configuré pour ce cycle");
        }

        // Montant en centimes
        long amountCents = price.multiply(BigDecimal.valueOf(100)).longValue();

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata("organization_id", orgId)
                    .putMetadata("plan_slug", planSlug)
                    .putMetadata("billing_cycle", billingCycle.name())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("eur")
                                                    .setUnitAmount(amountCents)
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(plan.getName() + " — " + billingCycle.name().toLowerCase())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            log.error("Erreur création session Stripe : {}", e.getMessage());
            throw new BusinessException("Impossible de créer la session de paiement");
        }
    }

    public void handleWebhookEvent(String rawBody, String sigHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("STRIPE_WEBHOOK_SECRET non configuré — événement ignoré");
            return;
        }
        Event event;
        try {
            event = Webhook.constructEvent(rawBody, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Signature Stripe invalide : {}", e.getMessage());
            throw new BusinessException("Signature invalide");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new BusinessException("Payload Stripe vide"));

            String orgId = session.getMetadata().get("organization_id");
            String planSlug = session.getMetadata().get("plan_slug");
            String billingCycleStr = session.getMetadata().get("billing_cycle");

            if (orgId == null || planSlug == null || billingCycleStr == null) {
                log.error("Métadonnées Stripe incomplètes : {}", session.getId());
                return;
            }

            BillingCycle billingCycle = BillingCycle.valueOf(billingCycleStr);
            subscriptionService.activateFromStripe(orgId, planSlug, billingCycle, session.getId());
            log.info("Abonnement activé via Stripe — org={} plan={}", orgId, planSlug);
        }
    }
}
