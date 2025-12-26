package com.lazyledger.storage;

import java.io.InputStream;
import java.time.OffsetDateTime;

public record StorageObject(String objectKey, InputStream inputStream, long contentLength, OffsetDateTime expiresAt) {
}
