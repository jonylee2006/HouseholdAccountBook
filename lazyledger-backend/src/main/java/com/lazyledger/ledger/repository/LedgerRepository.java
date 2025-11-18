package com.lazyledger.ledger.repository;

import com.lazyledger.ledger.domain.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
}
