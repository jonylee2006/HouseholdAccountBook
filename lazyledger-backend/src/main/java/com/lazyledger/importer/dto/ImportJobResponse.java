package com.lazyledger.importer.dto;

import com.lazyledger.common.enums.ImportJobStatus;
import com.lazyledger.common.enums.ImportSourceType;
import com.lazyledger.ledger.domain.ImportJob;

import java.time.OffsetDateTime;

import java.time.LocalDate;

public record ImportJobResponse(
        Long id,
        Long ledgerId,
        Long requestedBy,
        ImportSourceType sourceType,
        ImportJobStatus status,
        Integer totalCount,
        Integer successCount,
        Integer failureCount,
        String errorMessage,
        String objectKey,
        LocalDate statementDate,
        OffsetDateTime createdAt,
        OffsetDateTime completedAt
) {
    public static ImportJobResponse from(ImportJob job) {
        return new ImportJobResponse(
                job.getId(),
                job.getLedgerId(),
                job.getRequestedBy(),
                job.getSourceType(),
                job.getStatus(),
                job.getTotalCount(),
                job.getSuccessCount(),
                job.getFailureCount(),
                job.getErrorMessage(),
                job.getObjectKey(),
                job.getStatementDate(),
                job.getCreatedAt(),
                job.getCompletedAt()
        );
    }
}
