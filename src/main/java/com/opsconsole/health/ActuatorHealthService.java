package com.opsconsole.health;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

@Service
public class ActuatorHealthService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final HealthProperties healthProperties;

    public ActuatorHealthService(ObjectMapper objectMapper, HealthProperties healthProperties) {
        this.objectMapper = objectMapper;
        this.healthProperties = healthProperties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(healthProperties.getHealth().getConnectTimeoutMs());
        factory.setReadTimeout(healthProperties.getHealth().getReadTimeoutMs());
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public SystemHealthView check(MonitoredHost host) {
        Instant started = Instant.now();
        long startNanos = System.nanoTime();
        String probePath = resolveProbePath(host.getActuatorPath());

        try {
            HealthStatus status = probe(host, probePath);
            return buildView(host, status, null, started, startNanos, probePath);
        } catch (Exception primaryError) {
            String fallbackPath = "/actuator/health";
            if (!fallbackPath.equals(probePath)) {
                try {
                    HealthStatus status = probe(host, fallbackPath);
                    return buildView(host, status, null, started, startNanos, fallbackPath);
                } catch (Exception fallbackError) {
                    return buildView(host, HealthStatus.DOWN, describeError(fallbackError), started, startNanos, fallbackPath);
                }
            }
            return buildView(host, HealthStatus.DOWN, describeError(primaryError), started, startNanos, probePath);
        }
    }

    static String resolveProbePath(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            return "/actuator/health";
        }
        String path = configuredPath.startsWith("/") ? configuredPath : "/" + configuredPath;
        if ("/actuator".equals(path) || path.endsWith("/actuator")) {
            return "/actuator/health";
        }
        return path;
    }

    private HealthStatus probe(MonitoredHost host, String path) throws Exception {
        String url = host.baseUrl() + path;
        String body = restClient.get().uri(url).retrieve().body(String.class);
        if (body == null || body.isBlank()) {
            throw new RestClientException("Empty response from " + url);
        }
        return parseStatus(host, body);
    }

    HealthStatus parseStatus(MonitoredHost host, String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (root.has("status")) {
            return HealthStatus.fromActuator(root.get("status").asText());
        }
        if (host != null) {
            JsonNode healthLink = root.path("_links").path("health");
            if (healthLink.has("href")) {
                String healthPath = toProbePath(host, healthLink.get("href").asText());
                return probe(host, healthPath);
            }
        }
        throw new RestClientException("No status field in actuator response");
    }

    private static String toProbePath(MonitoredHost host, String href) {
        try {
            URI uri = URI.create(href);
            String path = uri.getPath();
            if (path != null && !path.isBlank()) {
                return path;
            }
        } catch (IllegalArgumentException ignored) {
            // use href as path below
        }
        if (href.startsWith(host.baseUrl())) {
            return href.substring(host.baseUrl().length());
        }
        return href.startsWith("/") ? href : "/" + href;
    }

    private static String describeError(Exception error) {
        if (error instanceof RestClientResponseException response) {
            return "HTTP " + response.getStatusCode().value() + ": " + response.getStatusText();
        }
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }

    private SystemHealthView buildView(
            MonitoredHost host,
            HealthStatus status,
            String errorMessage,
            Instant started,
            long startNanos,
            String probedPath
    ) {
        long responseMs = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
        return new SystemHealthView(
                host.getId(),
                host.getName(),
                host.getHost(),
                host.getPort(),
                host.getEnvironment(),
                host.getRegion(),
                status,
                status.displayLabel(),
                started,
                responseMs,
                errorMessage
        );
    }

    public int refreshSeconds() {
        return healthProperties.getHealth().getRefreshSeconds();
    }

    /** Test helper for JSON parsing without HTTP. */
    HealthStatus parseStatus(String body) throws Exception {
        return parseStatus(null, body);
    }
}
