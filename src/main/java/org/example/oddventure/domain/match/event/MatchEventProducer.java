package org.example.oddventure.domain.match.event;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.config.RabbitMQConfig;
import org.example.oddventure.domain.match.dto.event.MatchStartEventDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void produceMatchStartEvent(MatchStartEventDto dto) {
        long rawDelay = Duration.between(LocalDateTime.now(), dto.startTime()).toMillis();
        long delayMillis = Math.max(0, rawDelay); // 음수 방지 및 람다 적용을 위한 재할당

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAY_EXCHANGE,
                RabbitMQConfig.DELAY_ROUTING_KEY,
                dto,
                message -> {
                    message.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    return message;
                }
        );
    }
}
