package com.businesspilot.module.admin.service;

import com.businesspilot.module.admin.dto.AdminOrgResponse;
import com.businesspilot.module.admin.dto.AdminStatsResponse;
import com.businesspilot.module.auth.domain.Organization;
import com.businesspilot.module.auth.repository.OrganizationRepository;
import com.businesspilot.module.auth.repository.UserRepository;
import com.businesspilot.module.subscriptions.domain.Plan;
import com.businesspilot.module.subscriptions.domain.Subscription;
import com.businesspilot.module.subscriptions.repository.PlanRepository;
import com.businesspilot.module.subscriptions.repository.SubscriptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;
    private final SubscriptionRepository subRepo;
    private final PlanRepository planRepo;

    public AdminService(OrganizationRepository orgRepo, UserRepository userRepo,
                        SubscriptionRepository subRepo, PlanRepository planRepo) {
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.subRepo = subRepo;
        this.planRepo = planRepo;
    }

    public AdminStatsResponse getStats() {
        long totalOrgs = orgRepo.count();
        long activeOrgs = orgRepo.countByActiveTrue();
        long totalUsers = userRepo.count();

        List<Subscription> allSubs = subRepo.findAll();
        Map<UUID, Plan> plans = planRepo.findAll().stream()
                .collect(Collectors.toMap(Plan::getId, Function.identity()));

        Map<String, Long> byPlan = allSubs.stream()
                .collect(Collectors.groupingBy(
                        sub -> {
                            Plan p = plans.get(sub.getPlanId());
                            return p != null ? p.getName() : "Inconnu";
                        },
                        Collectors.counting()
                ));

        return new AdminStatsResponse(totalOrgs, activeOrgs, totalUsers, byPlan);
    }

    public Page<AdminOrgResponse> listOrganizations(Pageable pageable) {
        Page<Organization> orgs = orgRepo.findAll(pageable);

        List<String> orgIds = orgs.getContent().stream()
                .map(o -> o.getId().toString())
                .toList();

        Map<String, Subscription> subByOrgId = subRepo.findByOrganizationIdIn(orgIds).stream()
                .collect(Collectors.toMap(Subscription::getOrganizationId, Function.identity()));

        Map<UUID, Plan> plans = planRepo.findAll().stream()
                .collect(Collectors.toMap(Plan::getId, Function.identity()));

        return orgs.map(org -> {
            Subscription sub = subByOrgId.get(org.getId().toString());
            Plan plan = sub != null ? plans.get(sub.getPlanId()) : null;
            return new AdminOrgResponse(
                    org.getId(),
                    org.getName(),
                    org.getSlug(),
                    org.getEmail(),
                    org.getCountryCode(),
                    org.isActive(),
                    org.getCreatedAt(),
                    plan != null ? plan.getName() : "Gratuit",
                    plan != null ? plan.getSlug() : "free",
                    sub != null ? sub.getStatus().name() : "ACTIVE"
            );
        });
    }
}
