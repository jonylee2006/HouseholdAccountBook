package com.lazyledger.budget.repository;

import com.lazyledger.budget.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByLedgerIdAndYearMonth(Long ledgerId, String yearMonth);
}
