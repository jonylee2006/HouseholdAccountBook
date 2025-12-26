package com.lazyledger.ledger.stream;

import com.lazyledger.importer.event.StatementImportedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LedgerRealtimeNotifier {

    private final LedgerStreamService ledgerStreamService;

    public LedgerRealtimeNotifier(LedgerStreamService ledgerStreamService) {
        this.ledgerStreamService = ledgerStreamService;
    }

    @Async
    @EventListener
    public void handleImportEvent(StatementImportedEvent event) {
        ledgerStreamService.broadcast(event.getLedgerId(), Map.of(
                "type", "IMPORT_COMPLETED",
                "jobId", event.getJobId(),
                "transactionCount", event.getTransactionIds().size()
        ));
    }
}
