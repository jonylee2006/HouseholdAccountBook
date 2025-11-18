package com.lazyledger.importer.queue;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImportJobPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public ImportJobPublisher(RabbitTemplate rabbitTemplate,
                              @Value("${lazyledger.import.queue.exchange}") String exchange,
                              @Value("${lazyledger.import.queue.routing-key}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(ImportTaskMessage message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
