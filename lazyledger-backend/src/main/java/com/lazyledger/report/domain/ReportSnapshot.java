package com.lazyledger.report.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "report_snapshot", uniqueConstraints = {
        @UniqueConstraint(name = "uk_report_ledger_period", columnNames = {"ledgerId", "period"})
})
public class ReportSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ledgerId;

    @Column(nullable = false, length = 7)
    private String period;

    @Lob
    private String payload;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() {
        return id;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
