package org.example.oddventure.domain.ai.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageOutputSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());

        log.info("[Redis 구독] channel={}, message={}", channel, body);

        String[] parts = channel.split(":");
        String userId = parts[1];

        String destination = "/topic/chat/" + userId;
        messagingTemplate.convertAndSend(destination, body);
    }
}
