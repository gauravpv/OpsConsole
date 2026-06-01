package com.opsconsole.auth;

import java.util.Arrays;
import java.util.Optional;

public enum AppTab {
    DASHBOARD("dashboard", "Dashboard", "/"),
    HEALTH("health", "System Health", "/health"),
    API_TESTER("api-tester", "API Tester", "/api-tester"),
    ADMIN("admin", "System Admin", "/admin"),
    USERS("users", "User Admin", "/users"),
    LOGS("logs", "Logs Watch", "/logs");

    private final String id;
    private final String label;
    private final String path;

    AppTab(String id, String label, String path) {
        this.id = id;
        this.label = label;
        this.path = path;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public String path() {
        return path;
    }

    public static Optional<AppTab> fromId(String id) {
        return Arrays.stream(values()).filter(t -> t.id.equals(id)).findFirst();
    }
}
