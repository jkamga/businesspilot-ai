package com.businesspilot.module.widget.service;

import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.module.auth.domain.Organization;
import com.businesspilot.module.auth.repository.OrganizationRepository;
import com.businesspilot.module.conversations.domain.Conversation;
import com.businesspilot.module.conversations.domain.Message;
import com.businesspilot.module.conversations.domain.MessageRole;
import com.businesspilot.module.conversations.domain.MessageSource;
import com.businesspilot.module.conversations.dto.MessageResponse;
import com.businesspilot.module.conversations.repository.ConversationRepository;
import com.businesspilot.module.conversations.repository.MessageRepository;
import com.businesspilot.module.conversations.service.AiService;
import com.businesspilot.module.widget.dto.WidgetChatRequest;
import com.businesspilot.module.widget.dto.WidgetChatResponse;
import com.businesspilot.module.widget.dto.WidgetInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WidgetService {

    private final OrganizationRepository orgRepo;
    private final ConversationRepository convRepo;
    private final MessageRepository messageRepo;
    private final AiService aiService;

    public WidgetInfoResponse getInfo(UUID token) {
        Organization org = orgRepo.findByWidgetToken(token)
            .orElseThrow(() -> new NotFoundException("Widget token invalide"));
        return new WidgetInfoResponse(org.getId(), org.getName(),
            "Bonjour ! Comment puis-je vous aider ?");
    }

    @Transactional
    public WidgetChatResponse chat(UUID token, WidgetChatRequest req) {
        Organization org = orgRepo.findByWidgetToken(token)
            .orElseThrow(() -> new NotFoundException("Widget token invalide"));

        String orgId = org.getId().toString();

        Conversation conv;
        if (req.conversationId() != null) {
            conv = convRepo.findByIdAndOrganizationId(req.conversationId(), orgId)
                .orElseThrow(() -> new NotFoundException("Conversation introuvable"));
        } else {
            conv = new Conversation();
            conv.setOrganizationId(orgId);
            conv.setProspectName(req.visitorName() != null ? req.visitorName() : "Visiteur");
            conv.setSource(MessageSource.WIDGET);
            conv = convRepo.save(conv);
        }

        Message userMsg = new Message();
        userMsg.setConversationId(conv.getId());
        userMsg.setOrganizationId(org.getId());
        userMsg.setRole(MessageRole.USER);
        userMsg.setContent(req.message());
        userMsg.setSource(MessageSource.WIDGET);
        messageRepo.save(userMsg);

        List<MessageResponse> history = messageRepo
            .findAllByConversationIdOrderByCreatedAtAsc(conv.getId())
            .stream().map(MessageResponse::from).toList();

        String aiReply = aiService.generateResponse(org.getId(), req.message(), history);

        Message aiMsg = new Message();
        aiMsg.setConversationId(conv.getId());
        aiMsg.setOrganizationId(org.getId());
        aiMsg.setRole(MessageRole.ASSISTANT);
        aiMsg.setContent(aiReply);
        aiMsg.setSource(MessageSource.WIDGET);
        messageRepo.save(aiMsg);

        return new WidgetChatResponse(conv.getId(), aiReply);
    }
}
