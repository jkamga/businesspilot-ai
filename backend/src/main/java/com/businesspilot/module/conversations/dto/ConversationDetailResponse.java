package com.businesspilot.module.conversations.dto;

import java.util.List;

public record ConversationDetailResponse(
        ConversationResponse conversation,
        List<MessageResponse> messages
) {}
