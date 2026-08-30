package com.opsconsole.health.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "opsconsole")
public class HealthProperties {

    private final Health health = new Health();
    private final List<MonitorSeed> monitors = new ArrayList<>();

    public Health getHealth() {
        return health;
    }

    public ModelHub getModelHub() {
        return health.getModelHub();
    }

    public List<MonitorSeed> getMonitors() {
        return monitors;
    }

    public static class Health {
        private int refreshSeconds = 30;
        private int refreshIntervalMs = 30000;
        private int connectTimeoutMs = 3000;
        private int readTimeoutMs = 5000;
        private String defaultActuatorPath = "/actuator";
        private final ModelHub modelHub = new ModelHub();

        public ModelHub getModelHub() {
            return modelHub;
        }

        public int getRefreshSeconds() {
            return refreshSeconds;
        }

        public void setRefreshSeconds(int refreshSeconds) {
            this.refreshSeconds = refreshSeconds;
        }

        public int getRefreshIntervalMs() {
            return refreshIntervalMs;
        }

        public void setRefreshIntervalMs(int refreshIntervalMs) {
            this.refreshIntervalMs = refreshIntervalMs;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }

        public String getDefaultActuatorPath() {
            return defaultActuatorPath;
        }

        public void setDefaultActuatorPath(String defaultActuatorPath) {
            this.defaultActuatorPath = defaultActuatorPath;
        }
    }

    public static class ModelHub {
        private boolean enabled = false;
        private boolean mockMode = false;
        private final HubTarget uat = new HubTarget();
        private final HubTarget prod = new HubTarget();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isMockMode() {
            return mockMode;
        }

        public void setMockMode(boolean mockMode) {
            this.mockMode = mockMode;
        }

        public HubTarget getUat() {
            return uat;
        }

        public HubTarget getProd() {
            return prod;
        }

        /** Backward-compatible default: UAT base URL. */
        public String getBaseUrl() {
            return uat.getBaseUrl();
        }

        public void setBaseUrl(String baseUrl) {
            uat.setBaseUrl(baseUrl);
        }
    }

    public static class HubTarget {
        private String baseUrl = "https://emicardbre.bajajfinserv.in";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }

    public static class MonitorSeed {
        private String name;
        private String host;
        private int port;
        private String environment;
        private String region;
        private String actuatorPath;
        private String modelHubEnvironmentId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getActuatorPath() {
            return actuatorPath;
        }

        public void setActuatorPath(String actuatorPath) {
            this.actuatorPath = actuatorPath;
        }

        public String getModelHubEnvironmentId() {
            return modelHubEnvironmentId;
        }

        public void setModelHubEnvironmentId(String modelHubEnvironmentId) {
            this.modelHubEnvironmentId = modelHubEnvironmentId;
        }
    }
}
