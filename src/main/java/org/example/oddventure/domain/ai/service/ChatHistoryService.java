package org.example.oddventure.domain.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.ai.dto.ChatMessage;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatHistoryService {

    private static final int MAX_HISTORY = 20;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private String key(Long userId) {
        return "chat:history:" + userId;
    }

    public void addMessage(Long userId, String role, String content) {
        try {
            String json = objectMapper.writeValueAsString(new ChatMessage(role, content));
            redisTemplate.opsForList().rightPush(key(userId), json);
            redisTemplate.opsForList().trim(key(userId), -MAX_HISTORY, -1);
            redisTemplate.expire(key(userId), Duration.ofDays(7));
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: role={}, content={}, error={}", role, content, e.getMessage(), e);
        }
    }

    public List<String> getRecentMessages(Long userId) {
        return redisTemplate.opsForList().range(key(userId), -MAX_HISTORY, -1);
    }
}
