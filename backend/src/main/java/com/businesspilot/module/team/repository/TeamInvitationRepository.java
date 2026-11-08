package com.businesspilot.module.team.repository;

import com.businesspilot.module.team.domain.TeamInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, UUID> {
    Optional<TeamInvitation> findByToken(UUID token);
    List<TeamInvitation> findAllByOrganizationIdAndAcceptedAtIsNull(UUID orgId);
    boolean existsByOrganizationIdAndEmailAndAcceptedAtIsNull(UUID orgId, String email);
}
