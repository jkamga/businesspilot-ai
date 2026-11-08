package com.businesspilot.module.webhooks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WhatsAppInboundPayload(
        @JsonProperty("object") String object,
        @JsonProperty("entry") List<Entry> entry
) {
    public record Entry(
            @JsonProperty("changes") List<Change> changes
    ) {}

    public record Change(
            @JsonProperty("value") Value value
    ) {}

    public record Value(
            @JsonProperty("metadata") Metadata metadata,
            @JsonProperty("messages") List<WaMessage> messages,
            @JsonProperty("statuses") List<Object> statuses
    ) {}

    public record Metadata(
            @JsonProperty("phone_number_id") String phoneNumberId,
            @JsonProperty("display_phone_number") String displayPhoneNumber
    ) {}

    public record WaMessage(
            @JsonProperty("from") String from,
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("text") WaText text
    ) {}

    public record WaText(
            @JsonProperty("body") String body
    ) {}
}
