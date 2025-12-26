package com.lazyledger.ledger.service;

import com.lazyledger.common.enums.LedgerInviteStatus;
import com.lazyledger.common.enums.LedgerMemberStatus;
import com.lazyledger.ledger.domain.LedgerInvite;
import com.lazyledger.ledger.domain.LedgerMember;
import com.lazyledger.ledger.repository.LedgerInviteRepository;
import com.lazyledger.ledger.repository.LedgerMemberRepository;
import com.lazyledger.ledger.stream.LedgerStreamService;
import com.lazyledger.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LedgerInviteServiceTest {

    private LedgerInviteRepository inviteRepository;
    private LedgerMemberRepository memberRepository;
    private LedgerAccessService ledgerAccessService;
    private LedgerStreamService ledgerStreamService;
    private LedgerInviteService service;

    @BeforeEach
    void setUp() {
        inviteRepository = mock(LedgerInviteRepository.class);
        memberRepository = mock(LedgerMemberRepository.class);
        ledgerAccessService = mock(LedgerAccessService.class);
        ledgerStreamService = mock(LedgerStreamService.class);
        service = new LedgerInviteService(inviteRepository, memberRepository, ledgerAccessService, ledgerStreamService);
    }

    @Test
    void shouldCreateInvite() {
        UserPrincipal user = new UserPrincipal(1L, "u1");
        when(inviteRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LedgerInvite invite = service.createInvite(100L, user, Duration.ofHours(1));

        assertThat(invite.getLedgerId()).isEqualTo(100L);
        assertThat(invite.getInviterId()).isEqualTo(1L);
        assertThat(invite.getExpiredAt()).isAfter(OffsetDateTime.now());
    }

    @Test
    void shouldAcceptInviteAndCreateMember() {
        LedgerInvite invite = new LedgerInvite();
        invite.setLedgerId(200L);
        invite.setInviterId(2L);
        invite.setToken("abc");
        invite.setExpiredAt(OffsetDateTime.now().plusHours(1));
        invite.setStatus(LedgerInviteStatus.ACTIVE);
        when(inviteRepository.findByToken("abc")).thenReturn(Optional.of(invite));
        when(memberRepository.findByLedgerIdAndUserId(200L, 3L)).thenReturn(Optional.empty());

        service.acceptInvite("abc", new UserPrincipal(3L, "u3"));

        verify(memberRepository).save(any(LedgerMember.class));
        verify(inviteRepository).save(invite);
        verify(ledgerStreamService).broadcast(eq(200L), any());
    }
}
