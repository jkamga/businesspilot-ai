package com.businesspilot.module.dashboard.service;

import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.conversations.domain.ConversationStatus;
import com.businesspilot.module.conversations.repository.ConversationRepository;
import com.businesspilot.module.dashboard.dto.DashboardResponse;
import com.businesspilot.module.dashboard.dto.DashboardResponse.*;
import com.businesspilot.module.knowledge.domain.EntryStatus;
import com.businesspilot.module.knowledge.repository.KnowledgeEntryRepository;
import com.businesspilot.module.prospects.domain.ProspectStatus;
import com.businesspilot.module.prospects.repository.ProspectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ProspectRepository prospectRepo;
    private final ConversationRepository conversationRepo;
    private final KnowledgeEntryRepository knowledgeRepo;

    public DashboardService(ProspectRepository prospectRepo,
                            ConversationRepository conversationRepo,
                            KnowledgeEntryRepository knowledgeRepo) {
        this.prospectRepo = prospectRepo;
        this.conversationRepo = conversationRepo;
        this.knowledgeRepo = knowledgeRepo;
    }

    public DashboardResponse getStats() {
        String orgId = TenantContext.getCurrentTenant();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ProspectStatus s : ProspectStatus.values()) {
            byStatus.put(s.name(), prospectRepo.countByOrganizationIdAndStatus(orgId, s));
        }
        ProspectStats prospects = new ProspectStats(prospectRepo.countByOrganizationId(orgId), byStatus);

        ConversationStats conversations = new ConversationStats(
                conversationRepo.countByOrganizationId(orgId),
                conversationRepo.countByOrganizationIdAndStatus(orgId, ConversationStatus.OPEN),
                conversationRepo.countByOrganizationIdAndStatus(orgId, ConversationStatus.RESOLVED)
        );

        KnowledgeStats knowledge = new KnowledgeStats(
                knowledgeRepo.countByOrganizationId(orgId),
                knowledgeRepo.countByOrganizationIdAndStatus(orgId, EntryStatus.ACTIVE),
                knowledgeRepo.countByOrganizationIdAndStatus(orgId, EntryStatus.DRAFT)
        );

        List<RecentConversation> recentConversations = conversationRepo
                .findTop5ByOrganizationIdOrderByUpdatedAtDesc(orgId)
                .stream()
                .map(c -> new RecentConversation(c.getId(), c.getProspectName(), c.getProspectEmail(),
                        c.getStatus().name(), c.getUpdatedAt()))
                .toList();

        List<RecentProspect> recentProspects = prospectRepo
                .findTop5ByOrganizationIdOrderByUpdatedAtDesc(orgId)
                .stream()
                .map(p -> new RecentProspect(p.getId(), p.getFirstName(), p.getLastName(),
                        p.getCompany(), p.getStatus().name(), p.getUpdatedAt()))
                .toList();

        return new DashboardResponse(prospects, conversations, knowledge, recentConversations, recentProspects);
    }
}
