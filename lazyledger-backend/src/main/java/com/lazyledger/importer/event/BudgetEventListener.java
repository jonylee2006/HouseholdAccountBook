package com.lazyledger.importer.event;

import com.lazyledger.events.config.BudgetEventProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BudgetEventListener {

    private static final Logger log = LoggerFactory.getLogger(BudgetEventListener.class);

    private final RabbitTemplate rabbitTemplate;
    private final BudgetEventProperties budgetEventProperties;

    public BudgetEventListener(RabbitTemplate rabbitTemplate, BudgetEventProperties budgetEventProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.budgetEventProperties = budgetEventProperties;
    }

    @Async
    @EventListener
    public void handle(StatementImportedEvent event) {
        log.info("刷新预算统计 ledger={}, job={}", event.getLedgerId(), event.getJobId());
        Map<String, Object> payload = new HashMap<>();
        payload.put("ledgerId", event.getLedgerId());
        payload.put("jobId", event.getJobId());
        payload.put("transactionIds", event.getTransactionIds());
        rabbitTemplate.convertAndSend(budgetEventProperties.getExchange(), budgetEventProperties.getRoutingKey(), payload);
    }
}
