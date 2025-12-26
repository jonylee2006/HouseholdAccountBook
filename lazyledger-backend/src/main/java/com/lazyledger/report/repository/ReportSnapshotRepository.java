package com.lazyledger.report.repository;

import com.lazyledger.report.domain.ReportSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportSnapshotRepository extends JpaRepository<ReportSnapshot, Long> {

    Optional<ReportSnapshot> findByLedgerIdAndPeriod(Long ledgerId, String period);
}
