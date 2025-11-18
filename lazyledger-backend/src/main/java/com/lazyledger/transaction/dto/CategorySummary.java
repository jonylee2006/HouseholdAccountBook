package com.lazyledger.transaction.dto;

import java.math.BigDecimal;

public record CategorySummary(String category, BigDecimal amount) {
}
