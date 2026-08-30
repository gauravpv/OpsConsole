package com.opsconsole.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opsconsole.features")
public class OpsConsoleFeaturesProperties {

    private boolean apiTesterEnabled = false;

    public boolean isApiTesterEnabled() {
        return apiTesterEnabled;
    }

    public void setApiTesterEnabled(boolean apiTesterEnabled) {
        this.apiTesterEnabled = apiTesterEnabled;
    }
}
