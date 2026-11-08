package com.businesspilot.module.knowledge.controller;

import com.businesspilot.module.knowledge.domain.EntryStatus;
import com.businesspilot.module.knowledge.domain.EntryType;
import com.businesspilot.module.knowledge.dto.*;
import com.businesspilot.module.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Tag(name = "Knowledge Base", description = "Gestion de la base de connaissances")
@SecurityRequirement(name = "Bearer")
public class KnowledgeController {

    private final KnowledgeService service;

    @GetMapping
    @Operation(summary = "Lister les entrées (paginé)")
    public KnowledgePageResponse list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) EntryStatus status,
        @RequestParam(required = false) EntryType type
    ) {
        return service.list(page, size, status, type);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une entrée par ID")
    public KnowledgeEntryResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une entrée")
    public KnowledgeEntryResponse create(@Valid @RequestBody KnowledgeEntryRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une entrée")
    public KnowledgeEntryResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody KnowledgeEntryRequest request
    ) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publier une entrée (DRAFT → ACTIVE)")
    public KnowledgeEntryResponse publish(@PathVariable UUID id) {
        return service.publish(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une entrée")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche full-text (utilisé par l'IA)")
    public List<KnowledgeEntryResponse> search(
        @RequestParam String q,
        @RequestParam(defaultValue = "5") int limit
    ) {
        return service.search(q, limit);
    }
}
