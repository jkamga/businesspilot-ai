package com.businesspilot.module.subscriptions.repository;

import com.businesspilot.module.subscriptions.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByOrganizationId(String organizationId);

    List<Subscription> findByOrganizationIdIn(java.util.List<String> organizationIds);
}
