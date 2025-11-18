package org.example.oddventure.domain.match.unit;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.event.MatchNotificationConsumer;
import org.example.oddventure.domain.match.event.NotificationSubscriptionService;
import org.example.oddventure.domain.match.event.dto.MatchInfoUpdateDto;
import org.example.oddventure.domain.match.event.dto.MatchNotificationDto;
import org.example.oddventure.domain.match.event.dto.MatchOddsUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
public class MatchNotificationConsumerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private NotificationSubscriptionService subscriptionService;

    private MatchNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new MatchNotificationConsumer(messagingTemplate, subscriptionService);
    }

    @Test
    @DisplayName("매치 상태 변경 이벤트 발생 시 모든 사용자에게 알림이 전송된다")
    void consume_status() {
        // given
        MatchInfoUpdateDto payload = new MatchInfoUpdateDto(
                123L,
                "Galaxy Battle Phase 5",
                "Novaq",
                "Premdesant",
                LocalDateTime.now(),
                MatchStatus.ONGOING
        );
        MatchNotificationDto dto = new MatchNotificationDto(123L, "status", payload);

        // when
        consumer.consume(dto);

        // then
        verify(messagingTemplate).convertAndSend(
                "/topic/matches/123/status",
                payload
        );
    }

    @Test
    @DisplayName("배당률 변경 이벤트 발생 시 베팅한 사용자들에게만 알림이 전송된다")
    void consume_odds_withSubscribers() {
        // given
        MatchOddsUpdateDto payload = new MatchOddsUpdateDto(123L, "Novaq", new BigDecimal("1.5"));
        MatchNotificationDto dto = new MatchNotificationDto(123L, "odds", payload);

        Set<Long> subscribers = Set.of(10L, 20L);

        when(subscriptionService.getSubscribers(123L)).thenReturn(subscribers);

        // when
        consumer.consume(dto);

        // then
        verify(messagingTemplate).convertAndSend(
                "/user/10/queue/matches/123/odds",
                payload
        );
        verify(messagingTemplate).convertAndSend(
                "/user/20/queue/matches/123/odds",
                payload
        );
    }
}
