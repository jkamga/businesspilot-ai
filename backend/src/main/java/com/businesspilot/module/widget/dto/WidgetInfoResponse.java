package com.businesspilot.module.widget.dto;

import java.util.UUID;

public record WidgetInfoResponse(UUID orgId, String orgName, String welcomeMessage) {}
