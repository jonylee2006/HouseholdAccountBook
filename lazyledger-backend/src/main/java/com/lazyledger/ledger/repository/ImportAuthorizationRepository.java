package com.lazyledger.ledger.repository;

import com.lazyledger.ledger.domain.ImportAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportAuthorizationRepository extends JpaRepository<ImportAuthorization, Long> {

    List<ImportAuthorization> findByLedgerId(Long ledgerId);
}
