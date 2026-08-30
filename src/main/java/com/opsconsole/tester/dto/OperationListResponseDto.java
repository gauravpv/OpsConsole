package com.opsconsole.tester.dto;

import java.util.List;

public record OperationListResponseDto(
        String environment,
        String baseUrl,
        boolean mockMode,
        String description,
        String statusCode,
        String listEncryptionKey,
        String listEncryptionIv,
        String responseEncryptionKey,
        List<OperationEntryDto> operations
) {
}
