package com.lazyledger.ledger.repository;

import com.lazyledger.common.enums.ImportJobStatus;
import com.lazyledger.ledger.domain.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    List<ImportJob> findByLedgerIdAndStatusIn(Long ledgerId, List<ImportJobStatus> statuses);
}
