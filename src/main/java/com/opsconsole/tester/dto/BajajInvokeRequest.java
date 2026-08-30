package com.opsconsole.tester.dto;

public record BajajInvokeRequest(
        String environment,
        String publicUrl,
        String encryptionKey,
        String encryptionIv,
        String requestBody
) {
}
