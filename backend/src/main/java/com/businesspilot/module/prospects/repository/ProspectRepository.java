package com.businesspilot.module.prospects.repository;

import com.businesspilot.module.prospects.domain.Prospect;
import com.businesspilot.module.prospects.domain.ProspectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProspectRepository extends JpaRepository<Prospect, UUID> {

    Page<Prospect> findAllByOrganizationId(String organizationId, Pageable pageable);

    Page<Prospect> findAllByOrganizationIdAndStatus(String organizationId, ProspectStatus status, Pageable pageable);

    Optional<Prospect> findByIdAndOrganizationId(UUID id, String organizationId);

    @Query("SELECT p FROM Prospect p WHERE p.organizationId = :orgId " +
           "AND (LOWER(COALESCE(p.firstName,'')) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(COALESCE(p.lastName,'')) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(COALESCE(p.email,'')) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "OR LOWER(COALESCE(p.company,'')) LIKE LOWER(CONCAT('%',:q,'%')))")
    Page<Prospect> search(@Param("orgId") String orgId, @Param("q") String q, Pageable pageable);

    long countByOrganizationId(String organizationId);

    @Query("SELECT p FROM Prospect p WHERE p.status IN :statuses AND p.updatedAt < :threshold")
    List<Prospect> findStaleProspects(
        @Param("statuses") List<ProspectStatus> statuses,
        @Param("threshold") java.time.Instant threshold
    );

    long countByOrganizationIdAndStatus(String organizationId, ProspectStatus status);

    List<Prospect> findTop5ByOrganizationIdOrderByUpdatedAtDesc(String organizationId);
}
