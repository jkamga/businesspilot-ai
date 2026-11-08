package com.businesspilot.module.webhooks.service;

import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.auth.domain.Organization;
import com.businesspilot.module.auth.repository.OrganizationRepository;
import com.businesspilot.module.conversations.domain.Conversation;
import com.businesspilot.module.conversations.domain.ConversationStatus;
import com.businesspilot.module.conversations.domain.Message;
import com.businesspilot.module.conversations.domain.MessageRole;
import com.businesspilot.module.conversations.domain.MessageSource;
import com.businesspilot.module.conversations.dto.MessageResponse;
import com.businesspilot.module.conversations.repository.ConversationRepository;
import com.businesspilot.module.conversations.repository.MessageRepository;
import com.businesspilot.module.conversations.service.AiService;
import com.businesspilot.module.webhooks.dto.WhatsAppInboundPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class WhatsAppWebhookService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookService.class);

    @Value("${app.whatsapp.app-secret:}")
    private String appSecret;

    private final OrganizationRepository organizationRepo;
    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final AiService aiService;
    private final WhatsAppSenderService senderService;

    public WhatsAppWebhookService(OrganizationRepository organizationRepo,
                                  ConversationRepository conversationRepo,
                                  MessageRepository messageRepo,
                                  AiService aiService,
                                  WhatsAppSenderService senderService) {
        this.organizationRepo = organizationRepo;
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.aiService = aiService;
        this.senderService = senderService;
    }

    public boolean verifySignature(String rawBody, String signatureHeader) {
        if (appSecret == null || appSecret.isBlank()) return true; // skip in dev
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(computed);
            return expected.equals(signatureHeader);
        } catch (Exception e) {
            log.error("Erreur vérification HMAC : {}", e.getMessage());
            return false;
        }
    }

    @Async("whatsAppExecutor")
    @Transactional
    public void processAsync(WhatsAppInboundPayload payload) {
        if (payload.entry() == null) return;
        for (var entry : payload.entry()) {
            if (entry.changes() == null) continue;
            for (var change : entry.changes()) {
                var value = change.value();
                if (value == null || value.messages() == null || value.messages().isEmpty()) continue;

                String phoneNumberId = value.metadata() != null ? value.metadata().phoneNumberId() : null;
                if (phoneNumberId == null) continue;

                Organization org = organizationRepo.findByWhatsappPhoneNumberId(phoneNumberId).orElse(null);
                if (org == null) {
                    log.warn("Aucune organisation pour phoneNumberId={}", phoneNumberId);
                    continue;
                }

                TenantContext.setCurrentTenant(org.getId().toString());
                try {
                    for (var waMsg : value.messages()) {
                        handleMessage(org, waMsg);
                    }
                } finally {
                    TenantContext.clear();
                }
            }
        }
    }

    private void handleMessage(Organization org, WhatsAppInboundPayload.WaMessage waMsg) {
        if (!"text".equals(waMsg.type()) || waMsg.text() == null) return;

        String wamid = waMsg.id();
        if (messageRepo.existsByExternalId(wamid)) {
            log.debug("Message WhatsApp déjà traité : {}", wamid);
            return;
        }

        String fromPhone = waMsg.from();
        String orgId = org.getId().toString();

        Conversation conv = conversationRepo.findByOrganizationIdAndWhatsappPhone(orgId, fromPhone)
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setOrganizationId(orgId);
                    c.setWhatsappPhone(fromPhone);
                    c.setSource(MessageSource.WHATSAPP);
                    c.setProspectName(fromPhone);
                    c.setStatus(ConversationStatus.OPEN);
                    return conversationRepo.save(c);
                });

        Message userMsg = new Message();
        userMsg.setConversationId(conv.getId());
        userMsg.setOrganizationId(org.getId());
        userMsg.setRole(MessageRole.USER);
        userMsg.setContent(waMsg.text().body());
        userMsg.setSource(MessageSource.WHATSAPP);
        userMsg.setExternalId(wamid);
        messageRepo.save(userMsg);

        List<MessageResponse> history = messageRepo
                .findAllByConversationIdOrderByCreatedAtAsc(conv.getId())
                .stream().map(MessageResponse::from).toList();

        String aiText = aiService.generateResponse(org.getId(), waMsg.text().body(), history);

        Message aiMsg = new Message();
        aiMsg.setConversationId(conv.getId());
        aiMsg.setOrganizationId(org.getId());
        aiMsg.setRole(MessageRole.ASSISTANT);
        aiMsg.setContent(aiText);
        aiMsg.setSource(MessageSource.WHATSAPP);
        messageRepo.save(aiMsg);

        senderService.sendText(org.getWhatsappPhoneNumberId(), fromPhone, aiText);
    }
}
