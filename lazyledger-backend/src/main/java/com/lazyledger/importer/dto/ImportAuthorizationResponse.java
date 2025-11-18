package com.lazyledger.importer.dto;

import com.lazyledger.common.enums.ImportSourceType;
import java.time.OffsetDateTime;

public record ImportAuthorizationResponse(
        Long id,
        Long ledgerId,
        ImportSourceType sourceType,
        String displayName,
        OffsetDateTime expiresAt
) {
}
