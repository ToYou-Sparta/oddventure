package org.example.oddventure.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
    public static final String POINT_EXCHANGE = "bet.point.exchange";
    public static final String POINT_QUEUE = "bet.point.queue";
    public static final String POINT_ROUTING_KEY = "bet.point.adjust";

    @Bean
    public TopicExchange pointExchange() {
        return new TopicExchange(POINT_EXCHANGE);
    }

    @Bean
    public Queue pointQueue() {
        return new Queue(POINT_QUEUE, true);
    }

    @Bean
    public Binding pointBinding() {
        return BindingBuilder
                .bind(pointQueue())
                .to(pointExchange())
                .with(POINT_ROUTING_KEY);
    }

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(DELAY_EXCHANGE);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue delayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", REAL_ROUTING_KEY)
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

    @Bean
    public Binding realBinding() {
        return BindingBuilder.bind(realQueue())
                .to(dlxExchange())
                .with(REAL_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
