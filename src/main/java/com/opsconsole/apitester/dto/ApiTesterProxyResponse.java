package com.opsconsole.apitester.dto;

import java.util.Map;

public record ApiTesterProxyResponse(
        int status,
        String body,
        Map<String, String> headers,
        long durationMs,
        int sizeBytes,
        String error
) {
}
