package com.opsconsole.health;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "monitored_hosts")
public class MonitoredHost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int port;

    @Column(nullable = false)
    private String environment;

    @Column(nullable = false)
    private String region;

    @Column(name = "actuator_path", nullable = false)
    private String actuatorPath;

    @Column(nullable = false)
    private boolean enabled = true;

    protected MonitoredHost() {
    }

    public MonitoredHost(String name, String host, int port, String environment, String region, String actuatorPath) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.environment = environment;
        this.region = region;
        this.actuatorPath = actuatorPath;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getRegion() {
        return region;
    }

    public String getActuatorPath() {
        return actuatorPath;
    }

    public void setActuatorPath(String actuatorPath) {
        this.actuatorPath = actuatorPath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String baseUrl() {
        return "http://" + host + ":" + port;
    }
}
