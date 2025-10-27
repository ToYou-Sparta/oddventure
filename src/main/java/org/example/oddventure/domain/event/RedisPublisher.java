package org.example.oddventure.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 발행자
 * <p>
 * 서버 내부에서 발생한 이벤트를 Redis 채널로 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    // 지정한 채널로 메시지를 발행
    public void publish(String channel, Object message) {
        redisTemplate.convertAndSend(channel, message);
        log.info("[Redis 발행] channel={}, message={}", channel, message);
    }
}
