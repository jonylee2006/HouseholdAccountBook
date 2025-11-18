package com.lazyledger.importer.dto;

import com.lazyledger.common.enums.ImportSourceType;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record StatementImportRequest(
        @NotNull(message = "ledgerId 不能为空") Long ledgerId,
        @NotNull(message = "sourceType 不能为空") ImportSourceType sourceType,
        @NotBlank(message = "objectKey 不能为空") String objectKey,
        @NotNull(message = "statementDate 不能为空") LocalDate statementDate,
        String timezone
) {
    public String timezoneOrDefault() {
        return timezone == null || timezone.isBlank() ? "Asia/Shanghai" : timezone;
    }
}
