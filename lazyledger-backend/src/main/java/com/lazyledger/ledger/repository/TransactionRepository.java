package com.lazyledger.ledger.repository;

import com.lazyledger.ledger.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByImportJobId(Long importJobId);

    @Query("select coalesce(sum(t.amount), 0) from Transaction t where t.ledgerId = :ledgerId and t.direction = 'EXPENSE' and t.occurredAt between :start and :end")
    java.math.BigDecimal sumExpenseBetween(@Param("ledgerId") Long ledgerId,
                                           @Param("start") java.time.LocalDateTime start,
                                           @Param("end") java.time.LocalDateTime end);

    @Query("select new com.lazyledger.transaction.dto.CategorySummary(t.category, sum(t.amount)) " +
            "from Transaction t where t.ledgerId = :ledgerId and t.direction = 'EXPENSE' " +
            "and t.occurredAt between :start and :end group by t.category order by sum(t.amount) desc")
    List<com.lazyledger.transaction.dto.CategorySummary> topCategories(@Param("ledgerId") Long ledgerId,
                                                                       @Param("start") java.time.LocalDateTime start,
                                                                       @Param("end") java.time.LocalDateTime end);
}
