package com.lazyledger.ledger.domain;

import com.lazyledger.common.enums.LedgerInviteStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_invite", indexes = {
        @Index(name = "idx_invite_token", columnList = "token", unique = true)
})
public class LedgerInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ledgerId;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false)
    private Long inviterId;

    private Long acceptorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerInviteStatus status = LedgerInviteStatus.ACTIVE;

    @Column(nullable = false)
    private OffsetDateTime expiredAt;

    private OffsetDateTime acceptedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public Long getId() {
        return id;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getInviterId() {
        return inviterId;
    }

    public void setInviterId(Long inviterId) {
        this.inviterId = inviterId;
    }

    public Long getAcceptorId() {
        return acceptorId;
    }

    public void setAcceptorId(Long acceptorId) {
        this.acceptorId = acceptorId;
    }

    public LedgerInviteStatus getStatus() {
        return status;
    }

    public void setStatus(LedgerInviteStatus status) {
        this.status = status;
    }

    public OffsetDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(OffsetDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public OffsetDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(OffsetDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
