package com.businesspilot.module.prospects.controller;

import com.businesspilot.module.prospects.dto.*;
import com.businesspilot.module.prospects.service.ProspectService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/prospects")
@SecurityRequirement(name = "Bearer")
@Tag(name = "Prospects")
public class ProspectController {

    private final ProspectService service;

    public ProspectController(ProspectService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ProspectResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q) {
        return service.list(page, size, status, q);
    }

    @GetMapping("/{id}")
    public ProspectDetailResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProspectResponse create(@Valid @RequestBody ProspectRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ProspectResponse update(@PathVariable UUID id, @Valid @RequestBody ProspectRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public ProspectResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request) {
        return service.updateStatus(id, request);
    }

    @PostMapping("/{id}/interactions")
    @ResponseStatus(HttpStatus.CREATED)
    public InteractionResponse addInteraction(@PathVariable UUID id, @Valid @RequestBody InteractionRequest request) {
        return service.addInteraction(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
