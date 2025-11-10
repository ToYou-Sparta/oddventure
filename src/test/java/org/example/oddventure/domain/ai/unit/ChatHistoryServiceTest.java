package org.example.oddventure.domain.ai.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import org.example.oddventure.domain.ai.dto.ChatMessage;
import org.example.oddventure.domain.ai.service.ChatHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
public class ChatHistoryServiceTest {

    String key = "chat:history:";
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ListOperations<String, String> listOps;
    @InjectMocks
    private ChatHistoryService chatHistoryService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOps);
    }

    @Test
    @DisplayName("Redis에 채팅 메시지를 JSON 형태로 저장한다")
    void addMessage_success() throws Exception {
        // given
        Long userId = 1L;
        String role = "user";
        String userMessage = "오늘 경기 일정을 알려주세요!";
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        // when
        chatHistoryService.addMessage(userId, role, userMessage);

        // then
        verify(listOps).rightPush(eq(key + userId), captor.capture());
        verify(listOps).trim(key + userId, -20, -1);
        verify(redisTemplate).expire(key + userId, Duration.ofDays(7));

        // JSON 검증
        String savedJson = captor.getValue();
        ChatMessage parsed = objectMapper.readValue(savedJson, ChatMessage.class);
        assertThat(parsed.role()).isEqualTo(role);
        assertThat(parsed.content()).isEqualTo(userMessage);
    }

    @Test
    @DisplayName("Redis에서 최근 채팅 기록을 조회한다")
    void getRecentMessages_success() {
        // given
        Long userId = 1L;
        when(listOps.range(key + userId, -20, -1))
                .thenReturn(java.util.List.of("{\"role\":\"user\",\"content\":\"A팀이 이길까요?\"}"));

        // when
        List<String> result = chatHistoryService.getRecentMessages(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).contains("user");
    }
}
