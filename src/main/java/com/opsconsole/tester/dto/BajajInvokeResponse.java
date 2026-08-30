package com.opsconsole.tester.dto;

public record BajajInvokeResponse(
        int statusCode,
        long durationMs,
        int responseSizeBytes,
        String requestUrl,
        boolean mockMode,
        String decryptedBody,
        String error
) {
}
