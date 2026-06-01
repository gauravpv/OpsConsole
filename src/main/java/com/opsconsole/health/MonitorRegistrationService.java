package com.opsconsole.health;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MonitorRegistrationService {

    private final MonitoredHostRepository repository;
    private final SystemHealthMonitor healthMonitor;
    private final ActuatorHealthService healthService;
    private final HealthProperties healthProperties;

    public MonitorRegistrationService(
            MonitoredHostRepository repository,
            SystemHealthMonitor healthMonitor,
            ActuatorHealthService healthService,
            HealthProperties healthProperties
    ) {
        this.repository = repository;
        this.healthMonitor = healthMonitor;
        this.healthService = healthService;
        this.healthProperties = healthProperties;
    }

    @Transactional
    public HealthController.HealthRefreshResponse register(RegisterMonitorRequest request) {
        String name = requireText(request.name(), "Service name is required");
        String host = requireText(request.host(), "Host / IP is required");
        int port = requirePort(request.port());

        if (repository.existsByHostAndPort(host, port)) {
            throw new MonitorRegistrationException("A service is already registered for " + host + ":" + port);
        }

        String environment = normalizeEnvironment(requireText(request.environment(), "Environment is required"));
        String region = StringUtils.hasText(request.region()) ? request.region().trim() : "Unassigned";
        String actuatorPath = StringUtils.hasText(request.actuatorPath())
                ? request.actuatorPath().trim()
                : healthProperties.getHealth().getDefaultActuatorPath();

        MonitoredHost saved = repository.save(
                new MonitoredHost(name, host, port, environment, region, actuatorPath)
        );
        healthMonitor.refresh();

        return new HealthController.HealthRefreshResponse(
                healthMonitor.getSystems(),
                healthMonitor.summary(),
                healthMonitor.getLastRefreshedAt()
        );
    }

    private static String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new MonitorRegistrationException(message);
        }
        return value.trim();
    }

    private static int requirePort(Integer port) {
        if (port == null) {
            throw new MonitorRegistrationException("Port is required");
        }
        if (port < 1 || port > 65535) {
            throw new MonitorRegistrationException("Port must be between 1 and 65535");
        }
        return port;
    }

    static String normalizeEnvironment(String raw) {
        return switch (raw.trim().toLowerCase()) {
            case "prod", "production" -> "Production";
            case "uat" -> "UAT";
            case "dev", "development" -> "Development";
            default -> raw.trim();
        };
    }
}
