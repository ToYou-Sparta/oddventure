package org.example.oddventure.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 구독자
 * <p>
 * 수신된 메시지를 WebSocket을 통해 해당 채널을 구독 중인 프론트엔드 클라이언트에게 전달
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * RedisMessageListenerContainer가 Redis로부터 메시지를 받으면 호출
     *
     * @param message Redis에서 수신한 메시지 (body, channel 포함)
     * @param pattern Redis 구독 패턴 (예: match:123:odds, match:456:info)
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());

        log.info("[Redis 구독] channel={}, message={}", channel, body);

        String[] parts = channel.split(":");
        Long matchId = Long.parseLong(parts[1]);
        String category = parts[2];

        String destination = "/topic/matches/" + matchId + "/" + category;
        messagingTemplate.convertAndSend(destination, body);
    }
}
