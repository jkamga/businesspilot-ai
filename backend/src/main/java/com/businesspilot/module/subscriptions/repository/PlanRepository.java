package com.businesspilot.module.subscriptions.repository;

import com.businesspilot.module.subscriptions.domain.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

    List<Plan> findAllByActiveTrueOrderBySortOrderAsc();

    Optional<Plan> findBySlug(String slug);
}
