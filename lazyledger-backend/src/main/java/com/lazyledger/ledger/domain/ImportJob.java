package com.lazyledger.ledger.domain;

import com.lazyledger.common.enums.ImportJobStatus;
import com.lazyledger.common.enums.ImportSourceType;
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
@Table(name = "import_job")
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ledgerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportSourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportJobStatus status = ImportJobStatus.PENDING;

    private Integer totalCount;

    private Integer successCount;

    private Integer failureCount;

    private String errorMessage;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    private OffsetDateTime completedAt;

    public Long getId() {
        return id;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public ImportSourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(ImportSourceType sourceType) {
        this.sourceType = sourceType;
    }

    public ImportJobStatus getStatus() {
        return status;
    }

    public void setStatus(ImportJobStatus status) {
        this.status = status;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
