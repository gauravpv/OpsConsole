package com.opsconsole.health.controller;

import com.opsconsole.common.dto.ErrorResponse;
import com.opsconsole.health.domain.HealthDeploymentTier;
import com.opsconsole.health.domain.SystemHealthView;
import com.opsconsole.health.dto.HealthRefreshResponse;
import com.opsconsole.health.dto.ModelHubEnvironmentOption;
import com.opsconsole.health.dto.MonitorDetailsResponse;
import com.opsconsole.health.dto.RegisterMonitorRequest;
import com.opsconsole.health.exception.MonitorRegistrationException;
import com.opsconsole.health.service.ModelHubHealthService;
import com.opsconsole.health.service.MonitorRegistrationService;
import com.opsconsole.health.service.SystemHealthMonitor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/health")
public class HealthApiController {

    private final SystemHealthMonitor healthMonitor;
    private final MonitorRegistrationService registrationService;
    private final ModelHubHealthService modelHubHealthService;

    public HealthApiController(
            SystemHealthMonitor healthMonitor,
            MonitorRegistrationService registrationService,
            ModelHubHealthService modelHubHealthService
    ) {
        this.healthMonitor = healthMonitor;
        this.registrationService = registrationService;
        this.modelHubHealthService = modelHubHealthService;
    }

    @GetMapping("/systems")
    public List<SystemHealthView> systemsApi() {
        healthMonitor.refreshIfStale();
        return healthMonitor.getSystems();
    }

    @PostMapping("/refresh")
    public HealthRefreshResponse refreshNow() {
        healthMonitor.refresh();
        return new HealthRefreshResponse(
                healthMonitor.getSystems(),
                healthMonitor.summary(),
                healthMonitor.getLastRefreshedAt()
        );
    }

    @GetMapping("/monitors")
    public List<MonitorDetailsResponse> listMonitors() {
        return registrationService.listAll();
    }

    @PostMapping("/monitors")
    public HealthRefreshResponse registerMonitor(@RequestBody RegisterMonitorRequest request) {
        return registrationService.register(request);
    }

    @GetMapping("/monitors/{tier}/{id}")
    public MonitorDetailsResponse monitorDetails(@PathVariable String tier, @PathVariable Long id) {
        return registrationService.get(HealthDeploymentTier.fromPathSegment(tier), id);
    }

    @PutMapping("/monitors/{tier}/{id}")
    public HealthRefreshResponse updateMonitor(
            @PathVariable String tier,
            @PathVariable Long id,
            @RequestBody RegisterMonitorRequest request
    ) {
        return registrationService.update(HealthDeploymentTier.fromPathSegment(tier), id, request);
    }

    @DeleteMapping("/monitors/{tier}/{id}")
    public HealthRefreshResponse removeMonitor(@PathVariable String tier, @PathVariable Long id) {
        return registrationService.remove(HealthDeploymentTier.fromPathSegment(tier), id);
    }

    @GetMapping("/environments/{tier}")
    public List<ModelHubEnvironmentOption> environmentsForTier(@PathVariable String tier) {
        return modelHubHealthService.listEnvironments(HealthDeploymentTier.fromPathSegment(tier));
    }

    @GetMapping("/environments")
    public List<ModelHubEnvironmentOption> environmentsApi() {
        return modelHubHealthService.listEnvironments(HealthDeploymentTier.UAT);
    }

    @ExceptionHandler(MonitorRegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleRegistrationError(MonitorRegistrationException ex) {
        return new ErrorResponse(ex.getMessage());
    }
}
