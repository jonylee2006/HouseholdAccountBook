package com.lazyledger.ledger.service;

import com.lazyledger.common.enums.LedgerInviteStatus;
import com.lazyledger.common.enums.LedgerMemberStatus;
import com.lazyledger.common.enums.LedgerRole;
import com.lazyledger.common.exception.BusinessException;
import com.lazyledger.ledger.domain.LedgerInvite;
import com.lazyledger.ledger.domain.LedgerMember;
import com.lazyledger.ledger.repository.LedgerInviteRepository;
import com.lazyledger.ledger.repository.LedgerMemberRepository;
import com.lazyledger.ledger.stream.LedgerStreamService;
import com.lazyledger.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class LedgerInviteService {

    private static final Duration DEFAULT_EXPIRE = Duration.ofHours(24);

    private final LedgerInviteRepository ledgerInviteRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final LedgerAccessService ledgerAccessService;
    private final LedgerStreamService ledgerStreamService;

    public LedgerInviteService(LedgerInviteRepository ledgerInviteRepository,
                               LedgerMemberRepository ledgerMemberRepository,
                               LedgerAccessService ledgerAccessService,
                               LedgerStreamService ledgerStreamService) {
        this.ledgerInviteRepository = ledgerInviteRepository;
        this.ledgerMemberRepository = ledgerMemberRepository;
        this.ledgerAccessService = ledgerAccessService;
        this.ledgerStreamService = ledgerStreamService;
    }

    @Transactional
    public LedgerInvite createInvite(Long ledgerId, UserPrincipal user, Duration expireIn) {
        ledgerAccessService.ensureWritePermission(ledgerId, user.userId());
        LedgerInvite invite = new LedgerInvite();
        invite.setLedgerId(ledgerId);
        invite.setInviterId(user.userId());
        invite.setToken(LedgerInvite.generateToken());
        invite.setExpiredAt(OffsetDateTime.now().plus(expireIn == null ? DEFAULT_EXPIRE : expireIn));
        return ledgerInviteRepository.save(invite);
    }

    @Transactional
    public void acceptInvite(String token, UserPrincipal user) {
        LedgerInvite invite = ledgerInviteRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("INVITE_NOT_FOUND", "邀请不存在"));
        if (invite.getStatus() != LedgerInviteStatus.ACTIVE || invite.getExpiredAt().isBefore(OffsetDateTime.now())) {
            invite.setStatus(LedgerInviteStatus.EXPIRED);
            ledgerInviteRepository.save(invite);
            throw new BusinessException("INVITE_INVALID", "邀请已失效");
        }
        if (invite.getInviterId().equals(user.userId())) {
            throw new BusinessException("INVITE_SELF", "无需接受自己的邀请");
        }
        LedgerMember member = ledgerMemberRepository.findByLedgerIdAndUserId(invite.getLedgerId(), user.userId())
                .orElse(null);
        if (member != null && member.getStatus() == LedgerMemberStatus.ACTIVE) {
            throw new BusinessException("INVITE_ALREADY_MEMBER", "已经在账本中");
        }
        if (member == null) {
            member = new LedgerMember();
            member.setLedgerId(invite.getLedgerId());
            member.setUserId(user.userId());
            member.setRole(LedgerRole.MEMBER);
        }
        member.setStatus(LedgerMemberStatus.ACTIVE);
        ledgerMemberRepository.save(member);
        invite.setStatus(LedgerInviteStatus.USED);
        invite.setAcceptorId(user.userId());
        invite.setAcceptedAt(OffsetDateTime.now());
        ledgerInviteRepository.save(invite);
        ledgerStreamService.broadcast(invite.getLedgerId(), Map.of(
                "type", "MEMBER_JOINED",
                "userId", user.userId()
        ));
    }
}
