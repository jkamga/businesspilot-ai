package com.businesspilot.module.webhooks.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WhatsAppSenderService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppSenderService.class);

    private static final String GRAPH_API = "https://graph.facebook.com/v20.0";

    @Value("${app.whatsapp.api-token:}")
    private String apiToken;

    private final RestClient restClient;

    public WhatsAppSenderService() {
        this.restClient = RestClient.create();
    }

    public void sendText(String phoneNumberId, String toPhone, String text) {
        if (apiToken == null || apiToken.isBlank()) {
            log.warn("WHATSAPP_API_TOKEN non configuré — message non envoyé vers {}", toPhone);
            return;
        }
        try {
            restClient.post()
                    .uri(GRAPH_API + "/" + phoneNumberId + "/messages")
                    .header("Authorization", "Bearer " + apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "messaging_product", "whatsapp",
                            "to", toPhone,
                            "type", "text",
                            "text", Map.of("body", text)
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Erreur envoi WhatsApp vers {} : {}", toPhone, e.getMessage());
        }
    }
}
