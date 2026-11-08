package com.businesspilot.module.prospects.service;

import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.prospects.domain.Interaction;
import com.businesspilot.module.prospects.domain.Prospect;
import com.businesspilot.module.prospects.dto.*;
import com.businesspilot.module.prospects.repository.InteractionRepository;
import com.businesspilot.module.prospects.repository.ProspectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProspectService {

    private final ProspectRepository prospectRepo;
    private final InteractionRepository interactionRepo;

    public ProspectService(ProspectRepository prospectRepo, InteractionRepository interactionRepo) {
        this.prospectRepo = prospectRepo;
        this.interactionRepo = interactionRepo;
    }

    @Transactional(readOnly = true)
    public Page<ProspectResponse> list(int page, int size, String status, String q) {
        String orgId = TenantContext.getCurrentTenant();
        PageRequest pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());

        if (q != null && !q.isBlank()) {
            return prospectRepo.search(orgId, q.trim(), pageable).map(ProspectResponse::from);
        }
        if (status != null && !status.isBlank()) {
            var prospectStatus = com.businesspilot.module.prospects.domain.ProspectStatus.valueOf(status);
            return prospectRepo.findAllByOrganizationIdAndStatus(orgId, prospectStatus, pageable)
                    .map(ProspectResponse::from);
        }
        return prospectRepo.findAllByOrganizationId(orgId, pageable).map(ProspectResponse::from);
    }

    @Transactional(readOnly = true)
    public ProspectDetailResponse getById(UUID id) {
        String orgId = TenantContext.getCurrentTenant();
        Prospect p = findOrThrow(id, orgId);
        List<InteractionResponse> interactions = interactionRepo
                .findAllByProspectIdOrderByCreatedAtDesc(id)
                .stream().map(InteractionResponse::from).toList();
        return new ProspectDetailResponse(ProspectResponse.from(p), interactions);
    }

    public ProspectResponse create(ProspectRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        Prospect p = new Prospect();
        p.setOrganizationId(orgId);
        applyRequest(p, request);
        return ProspectResponse.from(prospectRepo.save(p));
    }

    public ProspectResponse update(UUID id, ProspectRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        Prospect p = findOrThrow(id, orgId);
        applyRequest(p, request);
        return ProspectResponse.from(prospectRepo.save(p));
    }

    public ProspectResponse updateStatus(UUID id, StatusUpdateRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        Prospect p = findOrThrow(id, orgId);
        p.setStatus(request.status());
        return ProspectResponse.from(prospectRepo.save(p));
    }

    public InteractionResponse addInteraction(UUID prospectId, InteractionRequest request) {
        String orgId = TenantContext.getCurrentTenant();
        findOrThrow(prospectId, orgId);
        Interaction i = new Interaction();
        i.setProspectId(prospectId);
        i.setOrganizationId(orgId);
        i.setType(request.type());
        i.setContent(request.content());
        return InteractionResponse.from(interactionRepo.save(i));
    }

    public void delete(UUID id) {
        String orgId = TenantContext.getCurrentTenant();
        Prospect p = findOrThrow(id, orgId);
        prospectRepo.delete(p);
    }

    private Prospect findOrThrow(UUID id, String orgId) {
        return prospectRepo.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new NotFoundException("Prospect introuvable"));
    }

    private void applyRequest(Prospect p, ProspectRequest req) {
        p.setFirstName(req.firstName());
        p.setLastName(req.lastName());
        p.setEmail(req.email());
        p.setPhone(req.phone());
        p.setCompany(req.company());
        p.setNotes(req.notes());
    }
}
