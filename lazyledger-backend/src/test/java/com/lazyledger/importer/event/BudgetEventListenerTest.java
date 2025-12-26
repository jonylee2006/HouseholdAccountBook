package com.lazyledger.importer.event;

import com.lazyledger.events.config.BudgetEventProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BudgetEventListenerTest {

    @Test
    void shouldPublishMessageToBudgetQueue() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        BudgetEventProperties properties = new BudgetEventProperties();
        properties.setExchange("budget.exchange");
        properties.setRoutingKey("budget.route");
        properties.setQueue("budget.queue");
        BudgetEventListener listener = new BudgetEventListener(rabbitTemplate, properties);
        StatementImportedEvent event = new StatementImportedEvent(this, 10L, 20L, List.of(100L, 200L));

        listener.handle(event);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq("budget.exchange"), eq("budget.route"), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).isInstanceOf(Map.class);
        Map<?, ?> payload = (Map<?, ?>) payloadCaptor.getValue();
        assertThat(payload.get("ledgerId")).isEqualTo(10L);
        assertThat(payload.get("jobId")).isEqualTo(20L);
        assertThat((List<?>) payload.get("transactionIds")).containsExactly(100L, 200L);
    }
}
