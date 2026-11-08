package com.businesspilot.module.widget.controller;

import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.auth.domain.Organization;
import com.businesspilot.module.auth.repository.OrganizationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings/ai")
@RequiredArgsConstructor
public class AiSettingsController {

    private final OrganizationRepository orgRepo;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAiSettings() {
        Organization org = findCurrentOrg();
        return ResponseEntity.ok(Map.of(
            "systemPrompt", org.getSystemPrompt() != null ? org.getSystemPrompt() : ""
        ));
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateAiSettings(
        @Valid @RequestBody AiSettingsRequest request
    ) {
        Organization org = findCurrentOrg();
        org.setSystemPrompt(request.systemPrompt());
        orgRepo.save(org);
        return ResponseEntity.ok(Map.of(
            "systemPrompt", org.getSystemPrompt() != null ? org.getSystemPrompt() : ""
        ));
    }

    private Organization findCurrentOrg() {
        String orgId = TenantContext.getCurrentTenant();
        return orgRepo.findById(UUID.fromString(orgId))
            .orElseThrow(() -> new NotFoundException("Organisation introuvable"));
    }

    public record AiSettingsRequest(@Size(max = 2000) String systemPrompt) {}
}
