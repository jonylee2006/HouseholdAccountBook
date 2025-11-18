package com.lazyledger.importer.dto;

import com.lazyledger.common.enums.ImportSourceType;
import jakarta.validation.constraints.NotNull;

public record StatementImportRequest(
        @NotNull(message = "ledgerId 不能为空") Long ledgerId,
        @NotNull(message = "sourceType 不能为空") ImportSourceType sourceType,
        String timezone
) {
    public String timezoneOrDefault() {
        return timezone == null || timezone.isBlank() ? "Asia/Shanghai" : timezone;
    }
}
