package org.example.oddventure.domain.match.event;

import static org.example.oddventure.common.config.RabbitMQConfig.MATCH_NOTIFY_EXCHANGE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.match.event.dto.MatchInfoUpdateDto;
import org.example.oddventure.domain.match.event.dto.MatchNotificationDto;
import org.example.oddventure.domain.match.event.dto.MatchOddsUpdateDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchNotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    // 경기 정보/상태 변경 알림
    public void sendMatchStatusChanged(MatchInfoUpdateDto dto) {
        MatchNotificationDto message = new MatchNotificationDto(dto.matchId(), "status", dto);
        String routingKey = "match." + dto.matchId() + ".status";

        rabbitTemplate.convertAndSend(MATCH_NOTIFY_EXCHANGE, routingKey, message);
        log.info("[RabbitMQ:MatchStatus] routingKey={}, payload={}", routingKey, dto);
    }

    // 배당률 변경 알림
    public void sendOddsChanged(MatchOddsUpdateDto dto) {
        MatchNotificationDto message = new MatchNotificationDto(dto.matchId(), "odds", dto);
        String routingKey = "match." + dto.matchId() + ".odds";

        rabbitTemplate.convertAndSend(MATCH_NOTIFY_EXCHANGE, routingKey, message);
        log.info("[RabbitMQ:Odds] routingKey={}, payload={}", routingKey, dto);
    }
}
