package com.opsconsole.health;

import com.opsconsole.health.config.HealthProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ModelHubYamlBindingTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @EnableConfigurationProperties(HealthProperties.class)
    static class TestConfig {
    }

    @Test
    void bindsModelHubUnderHealthPrefix() {
        runner.withPropertyValues(
                        "opsconsole.health.model-hub.enabled=true",
                        "opsconsole.health.model-hub.mock-mode=true",
                        "opsconsole.health.model-hub.uat.base-url=https://example-uat.test",
                        "opsconsole.health.model-hub.prod.base-url=https://example-prod.test")
                .run(context -> {
                    HealthProperties props = context.getBean(HealthProperties.class);
                    assertThat(props.getHealth().getModelHub().isEnabled()).isTrue();
                    assertThat(props.getHealth().getModelHub().isMockMode()).isTrue();
                    assertThat(props.getHealth().getModelHub().getUat().getBaseUrl()).isEqualTo("https://example-uat.test");
                    assertThat(props.getHealth().getModelHub().getProd().getBaseUrl()).isEqualTo("https://example-prod.test");
                });
    }
}
