package com.opsconsole.health;

public record RegisterMonitorRequest(
        String name,
        String host,
        Integer port,
        String environment,
        String region,
        String actuatorPath
) {
}
