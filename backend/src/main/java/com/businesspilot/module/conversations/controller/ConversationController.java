package com.businesspilot.module.conversations.controller;

import com.businesspilot.module.conversations.domain.ConversationStatus;
import com.businesspilot.module.conversations.dto.*;
import com.businesspilot.module.conversations.service.ConversationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@SecurityRequirement(name = "Bearer")
@Tag(name = "Conversations")
public class ConversationController {

    private final ConversationService service;

    public ConversationController(ConversationService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ConversationResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ConversationStatus status) {
        return service.list(page, size, status);
    }

    @GetMapping("/{id}")
    public ConversationDetailResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse create(@Valid @RequestBody ConversationRequest request) {
        return service.create(request);
    }

    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public List<MessageResponse> addMessage(
            @PathVariable UUID id,
            @Valid @RequestBody MessageRequest request) {
        return service.addMessage(id, request);
    }

    @PostMapping("/{id}/resolve")
    public ConversationResponse resolve(@PathVariable UUID id) {
        return service.resolve(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
