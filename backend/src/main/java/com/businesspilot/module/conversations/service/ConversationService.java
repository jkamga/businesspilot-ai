package com.businesspilot.module.conversations.service;

import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.common.plan.PlanLimitService;
import com.businesspilot.common.plan.PlanResource;
import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.conversations.domain.Conversation;
import com.businesspilot.module.conversations.domain.ConversationStatus;
import com.businesspilot.module.conversations.domain.Message;
import com.businesspilot.module.conversations.domain.MessageRole;
import com.businesspilot.module.conversations.dto.*;
import com.businesspilot.module.conversations.repository.ConversationRepository;
import com.businesspilot.module.conversations.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConversationService {

    private final ConversationRepository conversationRepo;
    private final MessageRepository messageRepo;
    private final AiService aiService;
    private final PlanLimitService planLimitService;

    public ConversationService(ConversationRepository conversationRepo,
                               MessageRepository messageRepo,
                               AiService aiService,
                               PlanLimitService planLimitService) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.aiService = aiService;
        this.planLimitService = planLimitService;
    }

    @Transactional(readOnly = true)
    public Page<ConversationResponse> list(int page, int size, ConversationStatus status) {
        String orgId = TenantContext.getCurrentTenant();
        PageRequest pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        if (status != null) {
            return conversationRepo.findAllByOrganizationIdAndStatus(orgId, status, pageable)
                    .map(ConversationResponse::from);
        }
        return conversationRepo.findAllByOrganizationId(orgId, pageable)
                .map(ConversationResponse::from);
    }

    @Transactional(readOnly = true)
    public ConversationDetailResponse getById(UUID id) {
        String orgId = TenantContext.getCurrentTenant();
        Conversation conv = conversationRepo.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new NotFoundException("Conversation introuvable"));
        List<MessageResponse> messages = messageRepo.findAllByConversationIdOrderByCreatedAtAsc(id)
                .stream().map(MessageResponse::from).toList();
        return new ConversationDetailResponse(ConversationResponse.from(conv), messages);
    }

    public ConversationResponse create(ConversationRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        planLimitService.checkLimit(orgId, PlanResource.CONVERSATION, conversationRepo.countByOrganizationId(orgId));
        Conversation conv = new Conversation();
        conv.setOrganizationId(orgId);
        conv.setProspectName(request.prospectName());
        conv.setProspectEmail(request.prospectEmail());
        return ConversationResponse.from(conversationRepo.save(conv));
    }

    public List<MessageResponse> addMessage(UUID conversationId, MessageRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        conversationRepo.findByIdAndOrganizationId(conversationId, orgId)
                .orElseThrow(() -> new NotFoundException("Conversation introuvable"));

        Message userMsg = new Message();
        userMsg.setConversationId(conversationId);
        userMsg.setOrganizationId(UUID.fromString(orgId));
        userMsg.setRole(MessageRole.USER);
        userMsg.setContent(request.content());
        messageRepo.save(userMsg);

        List<MessageResponse> history = messageRepo
                .findAllByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream().map(MessageResponse::from).toList();

        String aiText = aiService.generateResponse(UUID.fromString(orgId), request.content(), history);

        Message aiMsg = new Message();
        aiMsg.setConversationId(conversationId);
        aiMsg.setOrganizationId(UUID.fromString(orgId));
        aiMsg.setRole(MessageRole.ASSISTANT);
        aiMsg.setContent(aiText);
        messageRepo.save(aiMsg);

        return List.of(MessageResponse.from(userMsg), MessageResponse.from(aiMsg));
    }

    public ConversationResponse resolve(UUID id) {
        String orgId = TenantContext.getCurrentTenant();
        Conversation conv = conversationRepo.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new NotFoundException("Conversation introuvable"));
        conv.setStatus(ConversationStatus.RESOLVED);
        return ConversationResponse.from(conversationRepo.save(conv));
    }

    public MessageResponse addFeedback(UUID conversationId, UUID messageId, MessageFeedbackRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        conversationRepo.findByIdAndOrganizationId(conversationId, orgId)
                .orElseThrow(() -> new NotFoundException("Conversation introuvable"));
        Message msg = messageRepo.findById(messageId)
                .filter(m -> m.getOrganizationId().equals(UUID.fromString(orgId)))
                .orElseThrow(() -> new NotFoundException("Message introuvable"));
        msg.setFeedback(request.feedback());
        return MessageResponse.from(messageRepo.save(msg));
    }

    public void delete(UUID id) {
        String orgId = TenantContext.getCurrentTenant();
        Conversation conv = conversationRepo.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new NotFoundException("Conversation introuvable"));
        conversationRepo.delete(conv);
    }
}
