package com.businesspilot.module.prospects.service;

import com.businesspilot.module.prospects.domain.Prospect;
import com.businesspilot.module.prospects.domain.ProspectStatus;
import com.businesspilot.module.prospects.repository.ProspectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProspectAlertService {

    private final ProspectRepository prospectRepo;

    @Value("${app.alerts.stale-threshold-days:7}")
    private int staleThresholdDays;

    private static final List<ProspectStatus> ACTIVE_STATUSES = List.of(
        ProspectStatus.LEAD,
        ProspectStatus.CONTACTED,
        ProspectStatus.QUALIFIED
    );

    @Scheduled(cron = "${app.alerts.cron:0 0 9 * * *}")
    public void detectStaleProspects() {
        Instant threshold = Instant.now().minus(staleThresholdDays, ChronoUnit.DAYS);
        List<Prospect> stale = prospectRepo.findStaleProspects(ACTIVE_STATUSES, threshold);

        if (stale.isEmpty()) {
            return;
        }

        Map<String, Long> byOrg = stale.stream()
            .collect(Collectors.groupingBy(Prospect::getOrganizationId, Collectors.counting()));

        byOrg.forEach((orgId, count) ->
            log.warn("[PROSPECT_ALERT] org={} stale_count={} threshold_days={}", orgId, count, staleThresholdDays)
        );
    }
}
