package com.businesspilot.module.conversations.service;

import com.businesspilot.module.auth.repository.OrganizationRepository;
import com.businesspilot.module.conversations.dto.MessageResponse;
import com.businesspilot.module.knowledge.repository.KnowledgeEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${app.ai.provider-api-key:}")
    private String apiKey;

    @Value("${app.ai.provider-url:https://api.openai.com}")
    private String providerUrl;

    @Value("${app.ai.model:gpt-4o-mini}")
    private String model;

    @Value("${app.ai.max-tokens:1024}")
    private int maxTokens;

    @Value("${app.ai.fallback-message:L'assistant IA n'est pas configuré pour ce compte. Contactez votre administrateur.}")
    private String fallbackMessage;

    private static final int MAX_HISTORY_MESSAGES = 20;

    private final KnowledgeEntryRepository knowledgeRepo;
    private final OrganizationRepository orgRepo;
    private final RestClient restClient;

    public AiService(KnowledgeEntryRepository knowledgeRepo, OrganizationRepository orgRepo) {
        this.knowledgeRepo = knowledgeRepo;
        this.orgRepo = orgRepo;
        this.restClient = RestClient.create();
    }

    public String generateResponse(UUID orgId, String userMessage, List<MessageResponse> history) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallbackMessage;
        }

        String orgSystemPrompt = orgRepo.findById(orgId)
            .map(org -> org.getSystemPrompt())
            .orElse(null);

        String kbContext = buildKbContext(orgId, userMessage);
        String systemPrompt = buildSystemPrompt(kbContext, orgSystemPrompt);
        List<Map<String, String>> messages = buildMessages(systemPrompt, history, userMessage);

        try {
            Map<?, ?> response = restClient.post()
                    .uri(providerUrl + "/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", model,
                            "max_tokens", maxTokens,
                            "messages", messages
                    ))
                    .retrieve()
                    .body(Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (Exception e) {
            log.error("Erreur lors de l'appel à l'API AI: {}", e.getMessage());
            return "Je suis désolé, je rencontre des difficultés techniques. Veuillez réessayer dans quelques instants.";
        }
    }

    private String buildKbContext(UUID orgId, String query) {
        try {
            return knowledgeRepo.searchByOrganizationId(orgId.toString(), query, 3)
                    .stream()
                    .map(e -> "**" + e.getTitle() + "**\n" + e.getContent())
                    .reduce("", (a, b) -> a + "\n\n" + b)
                    .trim();
        } catch (Exception e) {
            log.warn("Impossible de récupérer le contexte KB: {}", e.getMessage());
            return "";
        }
    }

    private String buildSystemPrompt(String kbContext, String orgSystemPrompt) {
        String base = (orgSystemPrompt != null && !orgSystemPrompt.isBlank())
            ? orgSystemPrompt
            : """
              Tu es un assistant virtuel professionnel et bienveillant. \
              Réponds de façon concise, précise et utile. \
              Si tu ne connais pas la réponse, dis-le honnêtement plutôt qu'inventer.""";
        if (kbContext.isBlank()) return base;
        return base + "\n\nContexte de la base de connaissances :\n" + kbContext;
    }

    private List<Map<String, String>> buildMessages(String systemPrompt,
                                                    List<MessageResponse> history,
                                                    String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        int start = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        for (int i = start; i < history.size(); i++) {
            MessageResponse msg = history.get(i);
            String role = msg.role().name().equals("USER") ? "user" : "assistant";
            messages.add(Map.of("role", role, "content", msg.content()));
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        return messages;
    }
}
