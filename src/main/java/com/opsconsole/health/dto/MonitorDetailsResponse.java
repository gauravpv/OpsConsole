package com.opsconsole.health.dto;

import com.opsconsole.health.domain.HealthDeploymentTier;
import com.opsconsole.health.domain.MonitoredHost;
import com.opsconsole.health.domain.MonitoredHostProd;

public record MonitorDetailsResponse(
        Long id,
        String tier,
        String name,
        String host,
        int port,
        String environment,
        String region,
        String modelHubEnvironmentId,
        String actuatorPath
) {
    public static MonitorDetailsResponse fromUat(MonitoredHost host) {
        return new MonitorDetailsResponse(
                host.getId(),
                HealthDeploymentTier.UAT.id(),
                host.getName(),
                host.getHost(),
                host.getPort(),
                host.getEnvironment(),
                host.getRegion(),
                host.getModelHubEnvironmentId(),
                host.getActuatorPath()
        );
    }

    public static MonitorDetailsResponse fromProd(MonitoredHostProd host) {
        return new MonitorDetailsResponse(
                host.getId(),
                HealthDeploymentTier.PROD.id(),
                host.getName(),
                host.getHost(),
                host.getPort(),
                host.getEnvironment(),
                host.getRegion(),
                host.getModelHubEnvironmentId(),
                host.getActuatorPath()
        );
    }
}
