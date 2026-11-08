package com.businesspilot.module.widget.controller;

import com.businesspilot.module.widget.dto.WidgetChatRequest;
import com.businesspilot.module.widget.dto.WidgetChatResponse;
import com.businesspilot.module.widget.dto.WidgetInfoResponse;
import com.businesspilot.module.widget.service.WidgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/widget")
@RequiredArgsConstructor
public class WidgetController {

    private final WidgetService widgetService;

    @GetMapping("/{token}")
    public ResponseEntity<WidgetInfoResponse> info(@PathVariable UUID token) {
        return ResponseEntity.ok(widgetService.getInfo(token));
    }

    @PostMapping("/{token}/chat")
    public ResponseEntity<WidgetChatResponse> chat(
        @PathVariable UUID token,
        @Valid @RequestBody WidgetChatRequest req
    ) {
        return ResponseEntity.ok(widgetService.chat(token, req));
    }
}
