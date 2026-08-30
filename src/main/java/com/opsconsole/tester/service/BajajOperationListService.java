package com.opsconsole.tester.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsconsole.tester.config.BajajTesterProperties;
import com.opsconsole.tester.domain.BajajEnvironment;
import com.opsconsole.tester.dto.OperationEntryDto;
import com.opsconsole.tester.dto.OperationListResponseDto;
import com.opsconsole.tester.exception.BajajTesterException;
import com.opsconsole.tester.util.AesCbcCrypto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BajajOperationListService {

    private final BajajTesterProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public BajajOperationListService(BajajTesterProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public OperationListResponseDto fetchOperations(BajajEnvironment environment) {
        BajajTesterProperties.EnvironmentConfig config = configFor(environment);
        if (properties.isMockMode()) {
            return parseOperationList(loadMockPayload(environment), environment, config, true);
        }
        String plainJson = fetchLiveOperationList(config);
        return parseOperationList(plainJson, environment, config, false);
    }

    private BajajTesterProperties.EnvironmentConfig configFor(BajajEnvironment environment) {
        return environment == BajajEnvironment.PROD ? properties.getProd() : properties.getUat();
    }

    private String loadMockPayload(BajajEnvironment environment) {
        String resource = environment == BajajEnvironment.PROD
                ? "tester/operation-list-prod-mock.json"
                : "tester/operation-list-uat-mock.json";
        try {
            ClassPathResource classPathResource = new ClassPathResource(resource);
            if (!classPathResource.exists() && environment == BajajEnvironment.PROD) {
                classPathResource = new ClassPathResource("tester/operation-list-uat-mock.json");
            }
            return new String(classPathResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BajajTesterException("Failed to load mock operation list", ex);
        }
    }

    private String fetchLiveOperationList(BajajTesterProperties.EnvironmentConfig config) {
        try {
            String requestJson = objectMapper.writeValueAsString(config.getRequestBody());
            String encryptedBody = AesCbcCrypto.encryptUtf8(
                    requestJson,
                    config.getEncryptionKey(),
                    config.getEncryptionIv()
            );

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(config.operationListUrl()))
                    .timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(encryptedBody, StandardCharsets.UTF_8));

            for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
                if (header.getKey() != null && !header.getKey().isBlank()) {
                    builder.header(header.getKey(), header.getValue() != null ? header.getValue() : "");
                }
            }

            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BajajTesterException(
                        "Operation list request failed with HTTP " + response.statusCode()
                                + (response.body() != null && !response.body().isBlank()
                                ? ": " + abbreviate(response.body(), 240)
                                : "")
                );
            }

            return AesCbcCrypto.decryptUtf8(
                    response.body(),
                    config.getEncryptionKey(),
                    config.getEncryptionIv()
            );
        } catch (BajajTesterException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BajajTesterException("Failed to fetch operation list", ex);
        }
    }

    private OperationListResponseDto parseOperationList(
            String plainJson,
            BajajEnvironment environment,
            BajajTesterProperties.EnvironmentConfig config,
            boolean mockMode
    ) {
        try {
            JsonNode root = objectMapper.readTree(plainJson);
            JsonNode operationsNode = root.path("operationList");
            if (!operationsNode.isArray()) {
                throw new BajajTesterException("Operation list response missing operationList array");
            }

            List<OperationEntryDto> operations = new ArrayList<>(operationsNode.size());
            for (JsonNode node : operationsNode) {
                String publicUrl = text(node, "publicurl");
                operations.add(new OperationEntryDto(
                        publicUrl,
                        text(node, "slug"),
                        text(node, "appversion"),
                        text(node, "module"),
                        text(node, "apiversion"),
                        text(node, "hashcode"),
                        text(node, "salt"),
                        text(node, "hashcode32"),
                        text(node, "hashcode256"),
                        config.apiUrl(publicUrl),
                        text(node, "hashcode"),
                        text(node, "salt")
                ));
            }

            operations.sort((a, b) -> a.publicUrl().compareToIgnoreCase(b.publicUrl()));

            return new OperationListResponseDto(
                    environment.name(),
                    config.getBaseUrl(),
                    mockMode,
                    text(root, "description"),
                    text(root, "statusCode"),
                    config.getEncryptionKey(),
                    config.getEncryptionIv(),
                    text(root, "encryptionkey"),
                    operations
            );
        } catch (BajajTesterException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BajajTesterException("Failed to parse operation list response", ex);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }

    private static String abbreviate(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "…";
    }
}
