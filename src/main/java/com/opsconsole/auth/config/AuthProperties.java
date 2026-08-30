package com.opsconsole.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opsconsole.auth")
public class AuthProperties {

    /** dev = local sign-in buttons; azure = Microsoft Entra ID OAuth2. */
    private String mode = "dev";

    /** Default role code for first-time Azure AD sign-ins. */
    private String defaultRoleCode = "MONITORING";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isAzureMode() {
        return "azure".equalsIgnoreCase(mode);
    }

    public boolean isDevMode() {
        return !isAzureMode();
    }

    public String getDefaultRoleCode() {
        return defaultRoleCode;
    }

    public void setDefaultRoleCode(String defaultRoleCode) {
        this.defaultRoleCode = defaultRoleCode;
    }
}
