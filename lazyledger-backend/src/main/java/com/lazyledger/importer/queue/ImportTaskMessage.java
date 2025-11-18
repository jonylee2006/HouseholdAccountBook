package com.lazyledger.importer.queue;

import com.lazyledger.common.enums.ImportSourceType;

public record ImportTaskMessage(
        Long jobId,
        Long ledgerId,
        ImportSourceType sourceType,
        String objectKey,
        Long authorizationId
) {
}
