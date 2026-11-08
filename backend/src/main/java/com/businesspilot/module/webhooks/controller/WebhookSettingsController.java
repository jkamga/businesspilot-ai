package com.businesspilot.module.webhooks.controller;

import com.businesspilot.module.webhooks.dto.WebhookEndpointRequest;
import com.businesspilot.module.webhooks.dto.WebhookEndpointResponse;
import com.businesspilot.module.webhooks.service.WebhookService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/settings/webhooks")
@SecurityRequirement(name = "Bearer")
@Tag(name = "Settings")
public class WebhookSettingsController {

    private final WebhookService service;

    public WebhookSettingsController(WebhookService service) {
        this.service = service;
    }

    @GetMapping
    public List<WebhookEndpointResponse> list() {
        return service.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WebhookEndpointResponse create(@Valid @RequestBody WebhookEndpointRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public WebhookEndpointResponse update(@PathVariable UUID id,
                                          @Valid @RequestBody WebhookEndpointRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
