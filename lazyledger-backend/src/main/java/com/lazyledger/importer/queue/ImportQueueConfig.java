package com.lazyledger.importer.queue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ImportQueueConfig {

    @Value("${lazyledger.import.queue.name}")
    private String queueName;

    @Value("${lazyledger.import.queue.exchange}")
    private String exchangeName;

    @Value("${lazyledger.import.queue.routing-key}")
    private String routingKey;

    @Bean
    public Queue importQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange importExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding importBinding() {
        return BindingBuilder.bind(importQueue()).to(importExchange()).with(routingKey);
    }
}
