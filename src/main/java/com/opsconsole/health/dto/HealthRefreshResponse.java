package com.opsconsole.health.dto;

import com.opsconsole.health.domain.SystemHealthView;
import com.opsconsole.health.service.SystemHealthMonitor;

import java.time.Instant;
import java.util.List;

public record HealthRefreshResponse(
        List<SystemHealthView> systems,
        SystemHealthMonitor.HealthSummary summary,
        Instant lastRefreshedAt
) {
}
