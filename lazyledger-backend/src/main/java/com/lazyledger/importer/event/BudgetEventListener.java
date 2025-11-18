package com.lazyledger.importer.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BudgetEventListener {

    private static final Logger log = LoggerFactory.getLogger(BudgetEventListener.class);

    @Async
    @EventListener
    public void handle(StatementImportedEvent event) {
        log.info("刷新预算统计 ledger={}, job={}", event.getLedgerId(), event.getJobId());
        // TODO: 进一步实现预算统计刷新逻辑
    }
}
