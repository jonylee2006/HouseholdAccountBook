package com.lazyledger.storage;

import java.time.Instant;
import java.util.Map;

public record UploadSignatureResponse(
        String provider,
        String method,
        String url,
        String objectKey,
        Instant expiresAt,
        Map<String, String> headers
) {
}
