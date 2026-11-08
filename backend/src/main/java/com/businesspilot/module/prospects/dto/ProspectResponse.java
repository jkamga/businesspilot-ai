package com.businesspilot.module.prospects.dto;

import com.businesspilot.module.prospects.domain.Prospect;
import com.businesspilot.module.prospects.domain.ProspectStatus;

import java.time.Instant;
import java.util.UUID;

public record ProspectResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String company,
        ProspectStatus status,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProspectResponse from(Prospect p) {
        return new ProspectResponse(p.getId(), p.getFirstName(), p.getLastName(),
                p.getEmail(), p.getPhone(), p.getCompany(),
                p.getStatus(), p.getNotes(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
