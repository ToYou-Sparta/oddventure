package org.example.oddventure.domain.match.event;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSubscriptionService {

    private static final String SUBSCRIBER_KEY = "match:%d:subscribers";
    private final RedisTemplate<String, Object> redisTemplate;

    public void subscribeUserToMatch(Long userId, Long matchId) {
        String key = String.format(SUBSCRIBER_KEY, matchId);
        redisTemplate.opsForSet().add(key, userId);
    }

    public Set<Long> getSubscribers(Long matchId) {
        String key = String.format(SUBSCRIBER_KEY, matchId);

        Set<Object> members = redisTemplate.opsForSet().members(key);

        if (members == null || members.isEmpty()) {
            return Set.of();
        }

        return members.stream()
                .map(o -> Long.valueOf(o.toString()))
                .collect(Collectors.toSet());
    }
}
