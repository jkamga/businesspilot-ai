package com.businesspilot.module.prospects.dto;

import java.util.List;

public record ProspectDetailResponse(
        ProspectResponse prospect,
        List<InteractionResponse> interactions
) {}
