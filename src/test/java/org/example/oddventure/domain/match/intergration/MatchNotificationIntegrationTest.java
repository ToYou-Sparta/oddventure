package org.example.oddventure.domain.match.intergration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Map;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.event.MatchNotificationProducer;
import org.example.oddventure.domain.match.event.dto.MatchInfoUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MatchNotificationIntegrationTest {

    @Container
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management");

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MatchNotificationProducer matchNotificationProducer;

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
    }

    @Test
    @DisplayName("")
    void match_notification_flow() {
        // given
        MatchInfoUpdateDto dto = new MatchInfoUpdateDto(
                123L,
                "Galaxy Battle Phase 5",
                "Novaq",
                "Premdesant",
                LocalDateTime.now(),
                MatchStatus.ONGOING
        );

        // when
        matchNotificationProducer.sendMatchStatusChanged(dto);

        // then
        verify(messagingTemplate, timeout(2000)).convertAndSend(
                eq("/topic/matches/123/status"),
                any(Map.class)
        );
    }


}
