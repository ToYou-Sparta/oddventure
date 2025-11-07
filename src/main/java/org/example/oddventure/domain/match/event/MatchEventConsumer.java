package org.example.oddventure.domain.match.event;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.config.RabbitMQConfig;
import org.example.oddventure.domain.match.dto.event.MatchStartEventDto;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.service.MatchService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchEventConsumer {

    private final MatchService matchService;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.REAL_QUEUE)
    public void consumeMatchStartEvent(MatchStartEventDto dto) {
        matchService.updateStatus(dto.fetchId(), MatchStatus.ONGOING);
    }
}
