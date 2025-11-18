package com.lazyledger.ledger.service;

import com.lazyledger.common.enums.LedgerMemberStatus;
import com.lazyledger.common.enums.LedgerRole;
import com.lazyledger.common.exception.BusinessException;
import com.lazyledger.ledger.domain.LedgerMember;
import com.lazyledger.ledger.repository.LedgerMemberRepository;
import org.springframework.stereotype.Service;

@Service
public class LedgerAccessService {

    private final LedgerMemberRepository ledgerMemberRepository;

    public LedgerAccessService(LedgerMemberRepository ledgerMemberRepository) {
        this.ledgerMemberRepository = ledgerMemberRepository;
    }

    public LedgerMember ensureMember(Long ledgerId, Long userId) {
        LedgerMember member = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndStatus(ledgerId, userId, LedgerMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("LEDGER_MEMBER_NOT_FOUND", "不在该账本中"));
        return member;
    }

    public LedgerMember ensureWritePermission(Long ledgerId, Long userId) {
        LedgerMember member = ensureMember(ledgerId, userId);
        if (member.getRole() == LedgerRole.MEMBER) {
            throw new BusinessException("LEDGER_FORBIDDEN", "当前权限不足，需管理员以上角色");
        }
        return member;
    }
}
