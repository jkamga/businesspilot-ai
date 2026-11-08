package com.businesspilot.module.knowledge.repository;

import com.businesspilot.module.knowledge.domain.EntryStatus;
import com.businesspilot.module.knowledge.domain.EntryType;
import com.businesspilot.module.knowledge.domain.KnowledgeEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KnowledgeEntryRepository extends JpaRepository<KnowledgeEntry, UUID> {

    Page<KnowledgeEntry> findAllByOrganizationId(String organizationId, Pageable pageable);

    Page<KnowledgeEntry> findAllByOrganizationIdAndStatus(String organizationId, EntryStatus status, Pageable pageable);

    Page<KnowledgeEntry> findAllByOrganizationIdAndType(String organizationId, EntryType type, Pageable pageable);

    Optional<KnowledgeEntry> findByIdAndOrganizationId(UUID id, String organizationId);

    @Query(value = """
        SELECT * FROM knowledge_entries
        WHERE organization_id = :orgId
          AND status = 'ACTIVE'
          AND search_vector @@ plainto_tsquery('french', :query)
        ORDER BY ts_rank(search_vector, plainto_tsquery('french', :query)) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<KnowledgeEntry> searchByOrganizationId(
        @Param("orgId") String orgId,
        @Param("query") String query,
        @Param("limit") int limit
    );
}
