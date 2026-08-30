package com.opsconsole.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import com.opsconsole.health.domain.HealthDeploymentTier;
import com.opsconsole.health.service.ModelHubHealthService;
@SpringBootTest
@TestPropertySource(properties = {
        "opsconsole.health.model-hub.enabled=true",
        "opsconsole.health.model-hub.mock-mode=true"
})
class ModelHubIntegrationTest {

    @Autowired
    private ModelHubHealthService modelHubHealthService;

    @Test
    void fetchAll_returnsMockInstances() {
        assertThat(modelHubHealthService.isEnabled()).isTrue();
        assertThat(modelHubHealthService.isMockMode()).isTrue();
        var systems = modelHubHealthService.fetchAll(HealthDeploymentTier.UAT);
        assertThat(systems).isNotEmpty();
        assertThat(systems.stream().filter(s -> "bre_auth_uat".equals(s.environmentId())).count())
                .isGreaterThanOrEqualTo(2);
    }
}
