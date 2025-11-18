package com.lazyledger.importer.event;

import org.springframework.context.ApplicationEvent;

import java.util.List;

public class StatementImportedEvent extends ApplicationEvent {

    private final Long ledgerId;
    private final Long jobId;
    private final List<Long> transactionIds;

    public StatementImportedEvent(Object source, Long ledgerId, Long jobId, List<Long> transactionIds) {
        super(source);
        this.ledgerId = ledgerId;
        this.jobId = jobId;
        this.transactionIds = transactionIds;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public Long getJobId() {
        return jobId;
    }

    public List<Long> getTransactionIds() {
        return transactionIds;
    }
}
