package com.opsconsole.tester.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "opsconsole.bajaj-tester")
public class BajajTesterProperties {

    private boolean mockMode = true;
    private EnvironmentConfig uat = new EnvironmentConfig();
    private EnvironmentConfig prod = new EnvironmentConfig();

    public boolean isMockMode() {
        return mockMode;
    }

    public void setMockMode(boolean mockMode) {
        this.mockMode = mockMode;
    }

    public EnvironmentConfig getUat() {
        return uat;
    }

    public void setUat(EnvironmentConfig uat) {
        this.uat = uat;
    }

    public EnvironmentConfig getProd() {
        return prod;
    }

    public void setProd(EnvironmentConfig prod) {
        this.prod = prod;
    }

    public EnvironmentConfig forEnvironment(String environment) {
        if (environment != null && environment.equalsIgnoreCase("PROD")) {
            return prod;
        }
        return uat;
    }

    public static class EnvironmentConfig {
        private String baseUrl = "https://sauat.bajajfinserv.in/apis";
        private String operationListPath = "operationallist";
        private String encryptionKey = "2026Unpf7T7Mr4kNAHecXKolYoD9tiOT";
        private String encryptionIv = "2026JHNjiJSboivg";
        private Map<String, String> headers = new LinkedHashMap<>();
        private Map<String, String> requestBody = new LinkedHashMap<>();

        public String operationListUrl() {
            String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            String path = operationListPath.startsWith("/") ? operationListPath.substring(1) : operationListPath;
            return base + "/" + path;
        }

        public String apiUrl(String publicUrl) {
            String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            String path = publicUrl.startsWith("/") ? publicUrl.substring(1) : publicUrl;
            return base + "/" + path;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getOperationListPath() {
            return operationListPath;
        }

        public void setOperationListPath(String operationListPath) {
            this.operationListPath = operationListPath;
        }

        public String getEncryptionKey() {
            return encryptionKey;
        }

        public void setEncryptionKey(String encryptionKey) {
            this.encryptionKey = encryptionKey;
        }

        public String getEncryptionIv() {
            return encryptionIv;
        }

        public void setEncryptionIv(String encryptionIv) {
            this.encryptionIv = encryptionIv;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public Map<String, String> getRequestBody() {
            return requestBody;
        }

        public void setRequestBody(Map<String, String> requestBody) {
            this.requestBody = requestBody;
        }
    }
}
