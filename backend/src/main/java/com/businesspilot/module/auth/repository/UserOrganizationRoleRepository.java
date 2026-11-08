package com.businesspilot.module.auth.repository;

import com.businesspilot.module.auth.domain.UserOrganizationRole;
import com.businesspilot.module.auth.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserOrganizationRoleRepository extends JpaRepository<UserOrganizationRole, UUID> {
    Optional<UserOrganizationRole> findByUserIdAndOrganizationId(UUID userId, UUID organizationId);
    Optional<UserOrganizationRole> findFirstByUserId(UUID userId);

    @Query("SELECT r FROM UserOrganizationRole r WHERE r.organizationId = :orgId AND r.role IN :roles")
    List<UserOrganizationRole> findByOrganizationIdAndRoleIn(
            @Param("orgId") UUID orgId,
            @Param("roles") List<UserRole> roles);
}
