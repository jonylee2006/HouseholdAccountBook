package com.lazyledger.importer.model;

import com.lazyledger.common.enums.TransactionDirection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StatementRecord(
        LocalDateTime occurredAt,
        String merchantName,
        String subject,
        BigDecimal amount,
        TransactionDirection direction,
        String paymentMethod,
        String referenceId,
        String rawLine
) {
}
