package com.lazyledger.ledger.service;

import com.lazyledger.common.enums.LedgerMemberStatus;
import com.lazyledger.common.enums.LedgerRole;
import com.lazyledger.ledger.domain.Ledger;
import com.lazyledger.ledger.domain.LedgerMember;
import com.lazyledger.ledger.repository.LedgerMemberRepository;
import com.lazyledger.ledger.repository.LedgerRepository;
import com.lazyledger.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;

    public LedgerService(LedgerRepository ledgerRepository,
                         LedgerMemberRepository ledgerMemberRepository) {
        this.ledgerRepository = ledgerRepository;
        this.ledgerMemberRepository = ledgerMemberRepository;
    }

    @Transactional
    public Ledger createLedger(String name, String currencyCode, Double monthlyBudget, UserPrincipal owner) {
        Ledger ledger = new Ledger();
        ledger.setName(name);
        ledger.setOwnerId(owner.userId());
        ledger.setCurrencyCode(currencyCode != null ? currencyCode : "CNY");
        if (monthlyBudget != null) {
            ledger.setMonthlyBudget(monthlyBudget);
        }
        Ledger saved = ledgerRepository.save(ledger);
        LedgerMember member = new LedgerMember();
        member.setLedgerId(saved.getId());
        member.setUserId(owner.userId());
        member.setRole(LedgerRole.OWNER);
        member.setStatus(LedgerMemberStatus.ACTIVE);
        ledgerMemberRepository.save(member);
        return saved;
    }
}
