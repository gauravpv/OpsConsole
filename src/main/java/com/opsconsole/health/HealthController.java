package com.opsconsole.health;

import com.opsconsole.web.NavPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;
import java.util.List;

@Controller
public class HealthController {

    private final SystemHealthMonitor healthMonitor;
    private final ActuatorHealthService healthService;
    private final MonitorRegistrationService registrationService;

    public HealthController(
            SystemHealthMonitor healthMonitor,
            ActuatorHealthService healthService,
            MonitorRegistrationService registrationService
    ) {
        this.healthMonitor = healthMonitor;
        this.healthService = healthService;
        this.registrationService = registrationService;
    }

    @GetMapping("/health")
    public String systemHealth(Model model) {
        healthMonitor.refresh();
        model.addAttribute("activeNav", NavPage.HEALTH.id());
        model.addAttribute("systems", healthMonitor.getSystems());
        model.addAttribute("summary", healthMonitor.summary());
        model.addAttribute("refreshSeconds", healthService.refreshSeconds());
        model.addAttribute("lastRefreshedAt", healthMonitor.getLastRefreshedAt());
        return "system-health";
    }

    @GetMapping("/api/health/systems")
    @ResponseBody
    public List<SystemHealthView> systemsApi() {
        healthMonitor.refresh();
        return healthMonitor.getSystems();
    }

    @PostMapping("/api/health/refresh")
    @ResponseBody
    public HealthRefreshResponse refreshNow() {
        healthMonitor.refresh();
        return new HealthRefreshResponse(
                healthMonitor.getSystems(),
                healthMonitor.summary(),
                healthMonitor.getLastRefreshedAt()
        );
    }

    @PostMapping("/api/health/monitors")
    @ResponseBody
    public HealthRefreshResponse registerMonitor(@RequestBody RegisterMonitorRequest request) {
        return registrationService.register(request);
    }

    @ExceptionHandler(MonitorRegistrationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleRegistrationError(MonitorRegistrationException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    public record ErrorResponse(String message) {
    }

    public record HealthRefreshResponse(
            List<SystemHealthView> systems,
            SystemHealthMonitor.HealthSummary summary,
            Instant lastRefreshedAt
    ) {
    }
}
