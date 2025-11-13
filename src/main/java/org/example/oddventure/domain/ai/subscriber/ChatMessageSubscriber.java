package org.example.oddventure.domain.ai.subscriber;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.ai.service.ChatbotService;
import org.example.oddventure.domain.event.RedisPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageSubscriber implements MessageListener {

    private final ChatbotService chatbotService;
    private final RedisPublisher redisPublisher;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());

        log.info("[Redis 구독] channel={}, message={}", channel, body);

        String[] parts = channel.split(":");
        Long userId = Long.valueOf(parts[1]);
        String aiResponse = chatbotService.reply(userId, body);

        redisPublisher.publish("chat:" + userId + ":output", aiResponse);
    }
}
