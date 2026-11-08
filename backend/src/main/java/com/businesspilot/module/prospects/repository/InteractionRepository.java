package com.businesspilot.module.prospects.repository;

import com.businesspilot.module.prospects.domain.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InteractionRepository extends JpaRepository<Interaction, UUID> {

    List<Interaction> findAllByProspectIdOrderByCreatedAtDesc(UUID prospectId);
}
