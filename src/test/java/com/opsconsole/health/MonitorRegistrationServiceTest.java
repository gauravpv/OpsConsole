package com.opsconsole.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorRegistrationServiceTest {

    @Mock
    private MonitoredHostRepository repository;

    @Mock
    private SystemHealthMonitor healthMonitor;

    @Mock
    private ActuatorHealthService healthService;

    @Mock
    private HealthProperties healthProperties;

    @InjectMocks
    private MonitorRegistrationService registrationService;

    @Test
    void normalizeEnvironment_mapsCommonAliases() {
        assertThat(MonitorRegistrationService.normalizeEnvironment("prod")).isEqualTo("Production");
        assertThat(MonitorRegistrationService.normalizeEnvironment("dev")).isEqualTo("Development");
        assertThat(MonitorRegistrationService.normalizeEnvironment("UAT")).isEqualTo("UAT");
    }

    @Test
    void register_persistsHostAndRefreshesMonitor() {
        HealthProperties.Health health = new HealthProperties.Health();
        when(healthProperties.getHealth()).thenReturn(health);
        when(repository.existsByHostAndPort("10.0.0.8", 9090)).thenReturn(false);
        when(repository.save(any(MonitoredHost.class))).thenAnswer(invocation -> {
            MonitoredHost host = invocation.getArgument(0);
            return host;
        });
        when(healthMonitor.getSystems()).thenReturn(List.of());
        when(healthMonitor.summary()).thenReturn(new SystemHealthMonitor.HealthSummary(1, 0, 1, 1, 0));
        when(healthMonitor.getLastRefreshedAt()).thenReturn(Instant.now());

        var request = new RegisterMonitorRequest("Billing API", "10.0.0.8", 9090, "prod", "", null);
        registrationService.register(request);

        ArgumentCaptor<MonitoredHost> captor = ArgumentCaptor.forClass(MonitoredHost.class);
        verify(repository).save(captor.capture());
        MonitoredHost saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Billing API");
        assertThat(saved.getHost()).isEqualTo("10.0.0.8");
        assertThat(saved.getPort()).isEqualTo(9090);
        assertThat(saved.getEnvironment()).isEqualTo("Production");
        assertThat(saved.getRegion()).isEqualTo("Unassigned");
        assertThat(saved.getActuatorPath()).isEqualTo("/actuator");
        verify(healthMonitor).refresh();
    }

    @Test
    void register_rejectsDuplicateHostPort() {
        when(repository.existsByHostAndPort("localhost", 8080)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(
                new RegisterMonitorRequest("Dup", "localhost", 8080, "Development", "Local", null)
        )).isInstanceOf(MonitorRegistrationException.class)
                .hasMessageContaining("already registered");
    }
}
