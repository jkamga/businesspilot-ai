package com.businesspilot.module.auth.repository;

import com.businesspilot.module.auth.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    Optional<Organization> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByEmail(String email);
    long countByActiveTrue();

    java.util.Optional<Organization> findByWhatsappPhoneNumberId(String whatsappPhoneNumberId);
}
