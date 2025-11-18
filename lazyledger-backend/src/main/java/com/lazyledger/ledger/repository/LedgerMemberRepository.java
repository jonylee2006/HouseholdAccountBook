package com.lazyledger.ledger.repository;

import com.lazyledger.common.enums.LedgerMemberStatus;
import com.lazyledger.ledger.domain.LedgerMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LedgerMemberRepository extends JpaRepository<LedgerMember, Long> {

    Optional<LedgerMember> findByLedgerIdAndUserIdAndStatus(Long ledgerId, Long userId, LedgerMemberStatus status);
}
