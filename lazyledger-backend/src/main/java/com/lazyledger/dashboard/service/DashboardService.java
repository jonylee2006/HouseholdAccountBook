package com.lazyledger.dashboard.service;

import com.lazyledger.budget.repository.BudgetRepository;
import com.lazyledger.dashboard.dto.DashboardSummaryResponse;
import com.lazyledger.ledger.repository.TransactionRepository;
import com.lazyledger.ledger.service.LedgerAccessService;
import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import com.lazyledger.transaction.dto.CategorySummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final LedgerAccessService ledgerAccessService;
    private final CurrentUserService currentUserService;

    public DashboardService(TransactionRepository transactionRepository,
                            BudgetRepository budgetRepository,
                            LedgerAccessService ledgerAccessService,
                            CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.ledgerAccessService = ledgerAccessService;
        this.currentUserService = currentUserService;
    }

    public DashboardSummaryResponse summary(Long ledgerId) {
        UserPrincipal user = currentUserService.currentUser();
        ledgerAccessService.ensureMember(ledgerId, user.userId());
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay().minusNanos(1);
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay().minusNanos(1);

        BigDecimal todaySpent = transactionRepository.sumExpenseBetween(ledgerId, todayStart, todayEnd);
        BigDecimal monthSpent = transactionRepository.sumExpenseBetween(ledgerId, monthStart, monthEnd);
        BigDecimal budgetAmount = budgetRepository.findByLedgerIdAndYearMonth(ledgerId, currentMonth.toString())
                .map(budget -> budget.getAmount())
                .orElse(BigDecimal.ZERO);
        BigDecimal remaining = budgetAmount.subtract(monthSpent).max(BigDecimal.ZERO);
        List<CategorySummary> topCategories = transactionRepository.topCategories(ledgerId, monthStart, monthEnd)
                .stream()
                .limit(3)
                .toList();
        return new DashboardSummaryResponse(todaySpent, budgetAmount, remaining, topCategories);
    }
}
