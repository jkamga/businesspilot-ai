package com.businesspilot.module.prospects.service;

import com.businesspilot.module.auth.domain.UserRole;
import com.businesspilot.module.auth.repository.UserOrganizationRoleRepository;
import com.businesspilot.module.auth.repository.UserRepository;
import com.businesspilot.module.prospects.domain.Prospect;
import com.businesspilot.module.prospects.domain.ProspectStatus;
import com.businesspilot.module.prospects.repository.ProspectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProspectAlertService {

    private final ProspectRepository prospectRepo;
    private final UserOrganizationRoleRepository roleRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;

    @Value("${app.alerts.stale-threshold-days:7}")
    private int staleThresholdDays;

    @Value("${app.mail.from:noreply@businesspilot.app}")
    private String mailFrom;

    private static final List<ProspectStatus> ACTIVE_STATUSES = List.of(
        ProspectStatus.LEAD,
        ProspectStatus.CONTACTED,
        ProspectStatus.QUALIFIED
    );

    private static final List<UserRole> ADMIN_ROLES = List.of(UserRole.OWNER, UserRole.ADMIN);

    @Scheduled(cron = "${app.alerts.cron:0 0 9 * * *}")
    public void detectStaleProspects() {
        Instant threshold = Instant.now().minus(staleThresholdDays, ChronoUnit.DAYS);
        List<Prospect> stale = prospectRepo.findStaleProspects(ACTIVE_STATUSES, threshold);

        if (stale.isEmpty()) {
            return;
        }

        Map<String, Long> byOrg = stale.stream()
            .collect(Collectors.groupingBy(Prospect::getOrganizationId, Collectors.counting()));

        byOrg.forEach((orgId, count) -> {
            log.warn("[PROSPECT_ALERT] org={} stale_count={} threshold_days={}", orgId, count, staleThresholdDays);
            sendAlertEmails(orgId, count);
        });
    }

    private void sendAlertEmails(String orgId, long staleCount) {
        try {
            UUID orgUuid = UUID.fromString(orgId);
            List<String> adminEmails = roleRepo.findByOrganizationIdAndRoleIn(orgUuid, ADMIN_ROLES)
                .stream()
                .map(role -> userRepo.findById(role.getUserId()))
                .filter(java.util.Optional::isPresent)
                .map(opt -> opt.get().getEmail())
                .distinct()
                .toList();

            if (adminEmails.isEmpty()) {
                return;
            }

            String subject = "[BusinessPilot] " + staleCount + " prospect(s) inactif(s) depuis " + staleThresholdDays + " jours";
            String body = "Bonjour,\n\n" +
                staleCount + " prospect(s) de votre organisation n'ont pas eu d'activite depuis plus de " +
                staleThresholdDays + " jours.\n\n" +
                "Connectez-vous a BusinessPilot pour reprendre contact.\n\n" +
                "-- L'equipe BusinessPilot";

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(adminEmails.toArray(String[]::new));
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);

            log.info("[PROSPECT_ALERT] Email envoye a {} admin(s) pour org={}", adminEmails.size(), orgId);
        } catch (MailException e) {
            log.error("[PROSPECT_ALERT] Echec envoi email pour org={}: {}", orgId, e.getMessage());
        }
    }
}
