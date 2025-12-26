package com.lazyledger.importer.event;

import com.lazyledger.classification.service.TransactionClassificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ClassificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ClassificationEventListener.class);

    private final TransactionClassificationService classificationService;

    public ClassificationEventListener(TransactionClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @Async
    @EventListener
    public void handle(StatementImportedEvent event) {
        log.info("触发分类任务 ledger={}, job={}, records={}", event.getLedgerId(), event.getJobId(), event.getTransactionIds().size());
        classificationService.classify(event.getLedgerId(), event.getTransactionIds());
    }
}
