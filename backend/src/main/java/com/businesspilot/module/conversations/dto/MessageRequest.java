package com.businesspilot.module.conversations.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageRequest(@NotBlank String content) {}
