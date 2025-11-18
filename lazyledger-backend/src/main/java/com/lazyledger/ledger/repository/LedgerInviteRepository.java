package com.lazyledger.ledger.repository;

import com.lazyledger.ledger.domain.LedgerInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LedgerInviteRepository extends JpaRepository<LedgerInvite, Long> {

    Optional<LedgerInvite> findByToken(String token);
}
