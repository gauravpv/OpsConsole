package com.opsconsole.health;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MonitorCatalogInitializer implements ApplicationRunner {

    private final MonitoredHostRepository repository;
    private final HealthProperties properties;

    public MonitorCatalogInitializer(MonitoredHostRepository repository, HealthProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        normalizeLegacyActuatorPaths();

        if (repository.count() > 0) {
            return;
        }
        String defaultPath = ActuatorHealthService.resolveProbePath(properties.getHealth().getDefaultActuatorPath());
        for (HealthProperties.MonitorSeed seed : properties.getMonitors()) {
            String path = seed.getActuatorPath() != null && !seed.getActuatorPath().isBlank()
                    ? ActuatorHealthService.resolveProbePath(seed.getActuatorPath())
                    : defaultPath;
            repository.save(new MonitoredHost(
                    seed.getName(),
                    seed.getHost(),
                    seed.getPort(),
                    seed.getEnvironment(),
                    seed.getRegion(),
                    path
            ));
        }
    }

    private void normalizeLegacyActuatorPaths() {
        for (MonitoredHost host : repository.findAll()) {
            String resolved = ActuatorHealthService.resolveProbePath(host.getActuatorPath());
            if (!resolved.equals(host.getActuatorPath())) {
                host.setActuatorPath(resolved);
                repository.save(host);
            }
        }
    }
}
