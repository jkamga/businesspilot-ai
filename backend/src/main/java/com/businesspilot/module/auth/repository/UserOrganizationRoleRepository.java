package com.businesspilot.module.auth.repository;

import com.businesspilot.module.auth.domain.UserOrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserOrganizationRoleRepository extends JpaRepository<UserOrganizationRole, UUID> {
    Optional<UserOrganizationRole> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
    Optional<UserOrganizationRole> findFirstByUserId(UUID userId);
}
