package org.example.oddventure.common.config;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

    // Elasticsearch 동기화용
    public static final String ES_SYNC_EXCHANGE = "match.es.sync.exchange";
    public static final String ES_SYNC_QUEUE = "match.es.sync.queue";
    public static final String ES_SYNC_CREATED_KEY = "match.es.created";
    public static final String ES_SYNC_UPDATED_KEY = "match.es.updated";
    public static final String ES_SYNC_DELETED_KEY = "match.es.deleted";

    // 실시간 알림용
    public static final String MATCH_NOTIFY_EXCHANGE = "match.notify.exchange";
    public static final String MATCH_NOTIFY_QUEUE = "match.notify.queue";

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

    // Elasticsearch 동기화용 Exchange
    @Bean
    public TopicExchange esSyncExchange() {
        return new TopicExchange(ES_SYNC_EXCHANGE);
    }

    // Elasticsearch 동기화용 Queue (메시지 손실 방지 설정)
    @Bean
    public Queue esSyncQueue() {
        return QueueBuilder.durable(ES_SYNC_QUEUE)
                .maxLength(10000L)  // 최대 큐 크기 설정 (1만건)
                .overflow(QueueBuilder.Overflow.rejectPublish)  // 초과 시 publish 거부 (메시지 손실 방지)
                .build();
    }

    // Elasticsearch 동기화용 Binding (match.es.* 패턴 모두 수신)
    @Bean
    public Binding esSyncBinding() {
        return BindingBuilder
                .bind(esSyncQueue())
                .to(esSyncExchange())
                .with("match.es.*");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        // Publisher Confirms 활성화 (메시지 전송 성공 확인)
        rabbitTemplate.setMandatory(true);

        // Publisher Returns 처리 (라우팅 실패 시)
        rabbitTemplate.setReturnsCallback(returned -> {
            log.error("메시지 라우팅 실패 - exchange: {}, routingKey: {}, replyText: {}",
                    returned.getExchange(), returned.getRoutingKey(), returned.getReplyText());
        });

        return rabbitTemplate;
    }

    @Bean
    public TopicExchange matchNotifyExchange() {
        return new TopicExchange(MATCH_NOTIFY_EXCHANGE);
    }

    @Bean
    public Queue matchNotifyQueue() {
        return new Queue(MATCH_NOTIFY_QUEUE, true);
    }

    @Bean
    public Binding matchNotifyBinding() {
        return BindingBuilder
                .bind(matchNotifyQueue())
                .to(matchNotifyExchange())
                .with("match.*.*");
    }
}
