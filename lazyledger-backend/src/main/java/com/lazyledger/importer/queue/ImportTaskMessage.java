package com.lazyledger.importer.queue;

import com.lazyledger.common.enums.ImportSourceType;

import java.time.LocalDate;

public record ImportTaskMessage(
        Long jobId,
        Long ledgerId,
        ImportSourceType sourceType,
        String objectKey,
        Long authorizationId,
        LocalDate statementDate
) {
}
