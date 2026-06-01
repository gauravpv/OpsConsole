package com.opsconsole;

import com.opsconsole.health.HealthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(HealthProperties.class)
public class OpsConsoleApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsConsoleApplication.class, args);
    }
}
