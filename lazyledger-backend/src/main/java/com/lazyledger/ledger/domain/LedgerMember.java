package com.lazyledger.ledger.domain;

import com.lazyledger.common.enums.LedgerMemberStatus;
import com.lazyledger.common.enums.LedgerRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ledger_member")
public class LedgerMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ledgerId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerRole role = LedgerRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerMemberStatus status = LedgerMemberStatus.ACTIVE;

    @Column(nullable = false)
    private OffsetDateTime joinedAt = OffsetDateTime.now();

    public Long getId() {
        return id;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LedgerRole getRole() {
        return role;
    }

    public void setRole(LedgerRole role) {
        this.role = role;
    }

    public LedgerMemberStatus getStatus() {
        return status;
    }

    public void setStatus(LedgerMemberStatus status) {
        this.status = status;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(OffsetDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
