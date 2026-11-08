package com.businesspilot.module.prospects.dto;

import com.businesspilot.module.prospects.domain.ProspectStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(@NotNull ProspectStatus status) {}
