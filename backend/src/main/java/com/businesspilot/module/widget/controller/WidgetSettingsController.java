package com.businesspilot.module.widget.controller;

import com.businesspilot.common.exception.NotFoundException;
import com.businesspilot.common.tenant.TenantContext;
import com.businesspilot.module.auth.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings/widget")
@RequiredArgsConstructor
public class WidgetSettingsController {

    private final OrganizationRepository orgRepo;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getWidgetSettings() {
        String orgId = TenantContext.getCurrentTenant();
        var org = orgRepo.findById(UUID.fromString(orgId))
            .orElseThrow(() -> new NotFoundException("Organisation introuvable"));
        return ResponseEntity.ok(Map.of(
            "widgetToken", org.getWidgetToken(),
            "orgName", org.getName()
        ));
    }
}
