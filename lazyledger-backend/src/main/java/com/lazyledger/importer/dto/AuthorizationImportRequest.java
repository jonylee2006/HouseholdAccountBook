package com.lazyledger.importer.dto;

import jakarta.validation.constraints.NotNull;

public record AuthorizationImportRequest(
        @NotNull(message = "ledgerId 不能为空") Long ledgerId,
        @NotNull(message = "authorizationId 不能为空") Long authorizationId
) {
}
