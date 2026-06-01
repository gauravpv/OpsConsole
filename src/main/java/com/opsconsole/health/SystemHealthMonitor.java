package com.opsconsole.health;

import com.opsconsole.activity.ActivityFeedService;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SystemHealthMonitor {

    private final MonitoredHostRepository repository;
    private final ActuatorHealthService healthService;
    private final ActivityFeedService activityFeedService;
    private final HealthHistoryService healthHistoryService;
    private final Object refreshLock = new Object();
    private final AtomicReference<List<SystemHealthView>> latest =
            new AtomicReference<>(Collections.emptyList());
    private final Map<Long, HealthStatus> previousStatuses = new ConcurrentHashMap<>();
    private volatile Instant lastRefreshedAt;

    public SystemHealthMonitor(
            MonitoredHostRepository repository,
            ActuatorHealthService healthService,
            ActivityFeedService activityFeedService,
            HealthHistoryService healthHistoryService
    ) {
        this.repository = repository;
        this.healthService = healthService;
        this.activityFeedService = activityFeedService;
        this.healthHistoryService = healthHistoryService;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedRateString = "${opsconsole.health.refresh-interval-ms:30000}")
    public void scheduledRefresh() {
        refresh();
    }

    public void refresh() {
        synchronized (refreshLock) {
            List<SystemHealthView> results = repository.findByEnabledTrueOrderByNameAsc().parallelStream()
                    .map(healthService::check)
                    .toList();
            for (SystemHealthView system : results) {
                if (system.id() == null) {
                    continue;
                }
                HealthStatus previous = previousStatuses.get(system.id());
                if (previous != null && previous != system.status()) {
                    activityFeedService.recordHealthStatusChange(system, previous, system.status());
                }
                previousStatuses.put(system.id(), system.status());
            }
            latest.set(results);
            lastRefreshedAt = Instant.now();
            HealthSummary summary = summary();
            healthHistoryService.record(summary.total(), summary.up(), lastRefreshedAt);
        }
    }

    public HealthHistoryService.ServicesUpChart servicesUpChart() {
        HealthSummary summary = summary();
        return healthHistoryService.chartLast24Hours(summary.total(), summary.up());
    }

    public Instant getLastRefreshedAt() {
        return lastRefreshedAt;
    }

    public List<SystemHealthView> getSystems() {
        return latest.get();
    }

    public HealthSummary summary() {
        List<SystemHealthView> systems = latest.get();
        int up = 0;
        int down = 0;
        int production = 0;
        int uat = 0;
        for (SystemHealthView system : systems) {
            if (system.status() == HealthStatus.UP) {
                up++;
            } else if (system.status() == HealthStatus.DOWN) {
                down++;
            }
            if (isEnvironment(system.environment(), "Production")) {
                production++;
            }
            if (isEnvironment(system.environment(), "UAT")) {
                uat++;
            }
        }
        return new HealthSummary(systems.size(), up, down, production, uat);
    }

    private static boolean isEnvironment(String actual, String expected) {
        return actual != null && actual.equalsIgnoreCase(expected);
    }

    public record HealthSummary(int total, int up, int down, int production, int uat) {
        public double upPercent() {
            return total == 0 ? 0.0 : (up * 100.0 / total);
        }
    }
}
