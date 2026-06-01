package com.opsconsole.health;

import java.time.Instant;

public record SystemHealthView(
        Long id,
        String name,
        String host,
        int port,
        String environment,
        String region,
        HealthStatus status,
        String statusLabel,
        Instant lastChecked,
        long responseTimeMs,
        String errorMessage
) {
    public String subtitle() {
        return environment + " • " + region;
    }
}
