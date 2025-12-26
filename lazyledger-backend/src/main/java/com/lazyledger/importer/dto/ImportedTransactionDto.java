package com.lazyledger.importer.dto;

import com.lazyledger.common.enums.TransactionDirection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ImportedTransactionDto(
        LocalDateTime occurredAt,
        String merchantName,
        String subject,
        BigDecimal amount,
        TransactionDirection direction,
        String paymentMethod
) {
}
