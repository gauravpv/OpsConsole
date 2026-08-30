package com.opsconsole.apitester.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.opsconsole.apitester.dto.ApiTesterProxyRequest;
import com.opsconsole.apitester.dto.ApiTesterProxyResponse;
@Service
public class ApiTesterProxyService {

    private static final int MAX_RESPONSE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_METHODS = Set.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"
    );
    private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public ApiTesterProxyResponse execute(ApiTesterProxyRequest request) {
        String method = normalizeMethod(request.method());
        URI uri = parseUrl(request.url());
        long start = System.nanoTime();

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30));

            applyHeaders(builder, request.headers());

            String body = request.body() == null ? "" : request.body();
            if (BODY_METHODS.contains(method)) {
                builder.method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
            } else if ("GET".equals(method) || "HEAD".equals(method) || "DELETE".equals(method) || "OPTIONS".equals(method)) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported HTTP method: " + method);
            }

            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            String responseBody = response.body() == null ? "" : response.body();
            if (responseBody.getBytes(StandardCharsets.UTF_8).length > MAX_RESPONSE_BYTES) {
                responseBody = responseBody.substring(0, MAX_RESPONSE_BYTES) + "\n… [truncated]";
            }

            long durationMs = (System.nanoTime() - start) / 1_000_000;
            int sizeBytes = responseBody.getBytes(StandardCharsets.UTF_8).length;

            return new ApiTesterProxyResponse(
                    response.statusCode(),
                    responseBody,
                    flattenHeaders(response.headers().map()),
                    durationMs,
                    sizeBytes,
                    null
            );
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            long durationMs = (System.nanoTime() - start) / 1_000_000;
            return new ApiTesterProxyResponse(
                    0,
                    "",
                    Map.of(),
                    durationMs,
                    0,
                    ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()
            );
        }
    }

    private static String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "HTTP method is required");
        }
        String normalized = method.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_METHODS.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported HTTP method: " + method);
        }
        return normalized;
    }

    private static URI parseUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL is required");
        }
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL must use http or https");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL must include a host");
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL: " + ex.getMessage());
        }
    }

    private static void applyHeaders(HttpRequest.Builder builder, List<ApiTesterProxyRequest.HeaderEntry> headers) {
        if (headers == null) {
            return;
        }
        for (ApiTesterProxyRequest.HeaderEntry header : headers) {
            if (header == null || header.key() == null || header.key().isBlank()) {
                continue;
            }
            String key = header.key().trim();
            if (key.equalsIgnoreCase("host") || key.equalsIgnoreCase("content-length")) {
                continue;
            }
            builder.header(key, header.value() != null ? header.value() : "");
        }
    }

    private static Map<String, String> flattenHeaders(Map<String, List<String>> headers) {
        Map<String, String> flat = new LinkedHashMap<>();
        headers.forEach((key, values) -> flat.put(key, String.join(", ", values)));
        return flat;
    }
}
