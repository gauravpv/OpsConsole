package com.opsconsole.health;

import com.opsconsole.auth.NavAccessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HealthController.class)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SystemHealthMonitor healthMonitor;

    @MockitoBean
    private ActuatorHealthService healthService;

    @MockitoBean
    private MonitorRegistrationService registrationService;

    @MockitoBean
    private NavAccessService navAccessService;

    @Test
    void healthPage_loads() throws Exception {
        when(healthMonitor.getSystems()).thenReturn(List.of());
        when(healthMonitor.summary()).thenReturn(new SystemHealthMonitor.HealthSummary(0, 0, 0, 0, 0));
        when(healthService.refreshSeconds()).thenReturn(30);

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(view().name("system-health"))
                .andExpect(model().attribute("activeNav", "health"));

        verify(healthMonitor).refresh();
    }

    @Test
    void registerMonitorApi_works() throws Exception {
        when(registrationService.register(any())).thenReturn(
                new HealthController.HealthRefreshResponse(List.of(), new SystemHealthMonitor.HealthSummary(1, 1, 0, 0, 0), Instant.now())
        );

        mockMvc.perform(post("/api/health/monitors")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"name":"New Svc","host":"192.168.1.10","port":8081,"environment":"UAT","region":"US-East-1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.total").value(1));
    }

    @Test
    void refreshApi_works() throws Exception {
        when(healthMonitor.getSystems()).thenReturn(List.of());
        when(healthMonitor.summary()).thenReturn(new SystemHealthMonitor.HealthSummary(0, 0, 0, 0, 0));
        when(healthMonitor.getLastRefreshedAt()).thenReturn(Instant.now());

        mockMvc.perform(post("/api/health/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systems").isArray());
    }
}
