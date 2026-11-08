package com.businesspilot.module.knowledge.service;

import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.knowledge.domain.EntryStatus;
import com.businesspilot.module.knowledge.domain.EntryType;
import com.businesspilot.module.knowledge.domain.KnowledgeEntry;
import com.businesspilot.module.knowledge.dto.*;
import com.businesspilot.module.knowledge.repository.KnowledgeEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeEntryRepository repository;

    @Transactional(readOnly = true)
    public KnowledgePageResponse list(int page, int size, EntryStatus status, EntryType type) {
        String orgId = currentOrgId();
        PageRequest pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        Page<KnowledgeEntry> result;
        if (status != null) {
            result = repository.findAllByOrganizationIdAndStatus(orgId, status, pageable);
        } else if (type != null) {
            result = repository.findAllByOrganizationIdAndType(orgId, type, pageable);
        } else {
            result = repository.findAllByOrganizationId(orgId, pageable);
        }

        return new KnowledgePageResponse(
            result.getContent().stream().map(KnowledgeEntryResponse::from).toList(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.getNumber(),
            result.getSize()
        );
    }

    @Transactional(readOnly = true)
    public KnowledgeEntryResponse getById(UUID id) {
        return KnowledgeEntryResponse.from(findOwnedEntry(id));
    }

    @Transactional
    public KnowledgeEntryResponse create(KnowledgeEntryRequest req) {
        KnowledgeEntry entry = KnowledgeEntry.builder()
            .title(req.title())
            .content(req.content())
            .type(req.type())
            .status(EntryStatus.DRAFT)
            .wordCount(countWords(req.content()))
            .build();
        entry.setOrganizationId(currentOrgId());
        return KnowledgeEntryResponse.from(repository.save(entry));
    }

    @Transactional
    public KnowledgeEntryResponse update(UUID id, KnowledgeEntryRequest req) {
        KnowledgeEntry entry = findOwnedEntry(id);
        entry.setTitle(req.title());
        entry.setContent(req.content());
        entry.setType(req.type());
        entry.setWordCount(countWords(req.content()));
        return KnowledgeEntryResponse.from(repository.save(entry));
    }

    @Transactional
    public KnowledgeEntryResponse publish(UUID id) {
        KnowledgeEntry entry = findOwnedEntry(id);
        entry.setStatus(EntryStatus.ACTIVE);
        return KnowledgeEntryResponse.from(repository.save(entry));
    }

    @Transactional
    public void delete(UUID id) {
        KnowledgeEntry entry = findOwnedEntry(id);
        repository.delete(entry);
    }

    @Transactional(readOnly = true)
    public List<KnowledgeEntryResponse> search(String query, int limit) {
        return repository.searchByOrganizationId(currentOrgId(), query, limit)
            .stream().map(KnowledgeEntryResponse::from).toList();
    }

    private KnowledgeEntry findOwnedEntry(UUID id) {
        return repository.findByIdAndOrganizationId(id, currentOrgId())
            .orElseThrow(() -> new NotFoundException("Entrée introuvable : " + id));
    }

    private String currentOrgId() {
        String orgId = TenantContext.getCurrentTenant();
        if (orgId == null) throw new IllegalStateException("Tenant non initialisé");
        return orgId;
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
