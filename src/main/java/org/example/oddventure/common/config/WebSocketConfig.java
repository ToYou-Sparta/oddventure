package org.example.oddventure.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 * <p>
 * 클라이언트와 서버 간의 실시간 통신을 위해 WebSocket(STOMP)을 설정
 * <p>
 * /ws : 클라이언트가 서버와 최초로 WebSocket 연결을 맺는 엔드포인트<br> /app : 클라이언트가 서버로 메시지를 보낼 때 사용하는 경로<br> /topic : 서버가 클라이언트들에게 메시지를
 * 전달할 때 사용하는 경로
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}