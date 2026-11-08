package com.businesspilot.module.prospects.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ProspectRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Email @Size(max = 255) String email,
        @Size(max = 50) String phone,
        @Size(max = 255) String company,
        String notes
) {}
