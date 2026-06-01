package com.opsconsole.web;

public enum NavPage {
    DASHBOARD("dashboard"),
    HEALTH("health"),
    API_TESTER("api-tester"),
    ADMIN("admin"),
    USERS("users"),
    LOGS("logs");

    private final String id;

    NavPage(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
