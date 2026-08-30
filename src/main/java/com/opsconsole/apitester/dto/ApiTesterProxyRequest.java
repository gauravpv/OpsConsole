package com.opsconsole.apitester.dto;

import java.util.List;

public record ApiTesterProxyRequest(
        String method,
        String url,
        List<HeaderEntry> headers,
        String body
) {
    public record HeaderEntry(String key, String value) {
    }
}
