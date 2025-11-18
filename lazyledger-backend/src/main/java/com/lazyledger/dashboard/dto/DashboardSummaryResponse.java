package com.lazyledger.dashboard.dto;

import com.lazyledger.transaction.dto.CategorySummary;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        BigDecimal todaySpent,
        BigDecimal monthBudget,
        BigDecimal monthRemaining,
        List<CategorySummary> topCategories
) {
}
