package com.lazyledger.importer.dto;

import com.lazyledger.common.enums.ImportSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ImportAuthorizationRequest(
        @NotNull Long ledgerId,
        @NotNull ImportSourceType sourceType,
        @NotBlank String displayName,
        @NotBlank String accessToken,
        String refreshToken,
        OffsetDateTime expiresAt
) {
}
