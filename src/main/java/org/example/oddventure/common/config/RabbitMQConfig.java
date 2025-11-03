package org.example.oddventure.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String DELAY_EXCHANGE = "match.delay.exchange";
    public static final String DELAY_QUEUE = "match.delay.queue";
    public static final String DLX_EXCHANGE = "match.dlx.exchange";
    public static final String REAL_QUEUE = "match.real.queue";
    public static final String DELAY_ROUTING_KEY = "match.delay";
    public static final String REAL_ROUTING_KEY = "match.real";

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(DELAY_EXCHANGE);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public DirectExchange realExchange() {
        return new DirectExchange(REAL_QUEUE);
    }

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "match.real")
                .build();
    }

    @Bean
    public Queue realQueue() {
        return QueueBuilder.durable(REAL_QUEUE).build();
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue()).to(delayExchange()).with("match.delay");
    }
}
