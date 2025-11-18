package com.lazyledger.events.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(BudgetEventProperties.class)
public class BudgetEventConfiguration {

    @Bean
    public DirectExchange budgetExchange(BudgetEventProperties properties) {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    public Queue budgetQueue(BudgetEventProperties properties) {
        return new Queue(properties.getQueue(), true);
    }

    @Bean
    public Binding budgetBinding(DirectExchange budgetExchange, Queue budgetQueue, BudgetEventProperties properties) {
        return BindingBuilder.bind(budgetQueue).to(budgetExchange).with(properties.getRoutingKey());
    }
}
