package org.example.oddventure.domain.bet.event;

import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.config.RabbitMQConfig;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.bet.dto.PointEventDto;
import org.example.oddventure.domain.user.service.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BetEventConsumer {

    public final UserService userService;

    @RabbitListener(queues = RabbitMQConfig.POINT_QUEUE)
    public void consumePointEvent(PointEventDto dto) {
        PointAdjustRequest request = PointAdjustRequest.of(dto.betAmount(), "배당금 지급");
        userService.adjustUserPoints(dto.userId(), request);
    }
}
