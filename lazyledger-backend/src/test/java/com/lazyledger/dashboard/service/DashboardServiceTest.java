package com.lazyledger.dashboard.service;

import com.lazyledger.budget.domain.Budget;
import com.lazyledger.budget.repository.BudgetRepository;
import com.lazyledger.dashboard.dto.DashboardSummaryResponse;
import com.lazyledger.ledger.repository.TransactionRepository;
import com.lazyledger.ledger.service.LedgerAccessService;
import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import com.lazyledger.transaction.dto.CategorySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DashboardServiceTest {

    private TransactionRepository transactionRepository;
    private BudgetRepository budgetRepository;
    private LedgerAccessService ledgerAccessService;
    private CurrentUserService currentUserService;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        budgetRepository = mock(BudgetRepository.class);
        ledgerAccessService = mock(LedgerAccessService.class);
        currentUserService = mock(CurrentUserService.class);
        dashboardService = new DashboardService(transactionRepository, budgetRepository, ledgerAccessService, currentUserService);
        when(currentUserService.currentUser()).thenReturn(new UserPrincipal(1L, "u1"));
    }

    @Test
    void shouldReturnSummary() {
        when(transactionRepository.sumExpenseBetween(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("100.00"))
                .thenReturn(new BigDecimal("600.00"));
        Budget budget = new Budget();
        budget.setLedgerId(100L);
        budget.setYearMonth(java.time.YearMonth.now().toString());
        budget.setAmount(new BigDecimal("1000"));
        when(budgetRepository.findByLedgerIdAndYearMonth(eq(100L), any())).thenReturn(Optional.of(budget));
        when(transactionRepository.topCategories(eq(100L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new CategorySummary("餐饮", new BigDecimal("200"))));

        DashboardSummaryResponse response = dashboardService.summary(100L);

        assertThat(response.todaySpent()).isEqualByComparingTo("100.00");
        assertThat(response.monthBudget()).isEqualByComparingTo("1000");
        assertThat(response.monthRemaining()).isEqualByComparingTo("400");
        assertThat(response.topCategories()).hasSize(1);
    }
}
