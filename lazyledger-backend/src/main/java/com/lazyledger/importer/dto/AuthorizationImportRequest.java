package com.lazyledger.importer.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AuthorizationImportRequest(
        @NotNull(message = "ledgerId 不能为空") Long ledgerId,
        @NotNull(message = "authorizationId 不能为空") Long authorizationId,
        @NotNull(message = "statementDate 不能为空") LocalDate statementDate
) {
}
