package org.example.oddventure.domain.match.event;

import static org.example.oddventure.common.config.RabbitMQConfig.MATCH_NOTIFY_QUEUE;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.match.event.dto.MatchNotificationDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class MatchNotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationSubscriptionService subscriptionService;

    @RabbitListener(queues = MATCH_NOTIFY_QUEUE)
    public void consume(MatchNotificationDto dto) {
        if (dto.category().equals("odds")) {
            notifyOddsSubscribers(dto);
        } else {
            String destination = "/topic/matches/" + dto.matchId() + "/status";

            messagingTemplate.convertAndSend(destination, dto.payload());
            log.info("[RabbitMQ:MatchStatus] matchId={}, destination={}, payload={}", dto.matchId(), destination,
                    dto.payload());
        }
    }

    // 배당률 변경은 구독한 유저에게만 전송
    private void notifyOddsSubscribers(MatchNotificationDto dto) {
        Set<Long> subscribers = subscriptionService.getSubscribers(dto.matchId());

        if (subscribers.isEmpty()) {
            log.warn("[RabbitMQ:Odds] 알림을 받을 구독자가 없습니다.");
            return;
        }

        for (Long userId : subscribers) {
            String destination = "/user/" + userId + "/queue/matches/" + dto.matchId() + "/odds";

            messagingTemplate.convertAndSend(destination, dto.payload());
        }

        log.info("[RabbitMQ:Odds] matchId={}, sent to {} subscribers, payload={}", dto.matchId(), subscribers.size(),
                dto.payload());
    }
}
