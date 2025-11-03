package org.example.oddventure.domain.bet.event;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.config.RabbitMQConfig;
import org.example.oddventure.domain.bet.dto.PointEventDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BetEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void producePointEvent(PointEventDto dto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.POINT_EXCHANGE, RabbitMQConfig.DELAY_ROUTING_KEY, dto);
    }
}
