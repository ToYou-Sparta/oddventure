package org.example.oddventure.domain.ai.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.oddventure.domain.ai.service.ChatHistoryService;
import org.example.oddventure.domain.ai.service.ChatbotService;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

@ExtendWith(MockitoExtension.class)
public class ChatbotServiceTest {

    @Mock
    private ChatHistoryService chatHistoryService;

    @Mock
    private ChatClient chatClient;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private ChatbotService chatbotService;

    @Test
    @DisplayName("일반 대화 시 히스토리를 포함해 응답을 생성한다")
    void reply_success() {
        // given
        Long userId = 1L;
        String userMessage = "오늘은 어떤 경기가 있나요?";
        when(chatHistoryService.getRecentMessages(userId)).thenReturn(List.of("이전대화1", "이전대화2"));
        mockChatClientContent("안녕하세요! 오늘은 FaZe Clan과 Team Vitality의 경기가 있어요.");

        // when
        String reply = chatbotService.reply(userId, userMessage);

        // then
        assertThat(reply).isNotNull();
        assertThat(reply).contains("오늘").contains("경기");
        verify(chatHistoryService).addMessage(userId, "user", userMessage);
        verify(chatHistoryService).addMessage(userId, "assistant", "안녕하세요! 오늘은 FaZe Clan과 Team Vitality의 경기가 있어요.");
        verify(chatHistoryService, times(2)).addMessage(eq(userId), anyString(), anyString());
    }

    @Test
    @DisplayName("'승률' 키워드가 있으면 승률 분기 로직을 탄다")
    void reply_winningRateBranch() {
        // given
        Long userId = 1L;
        when(matchRepository.findByWinnerIsNotNull()).thenReturn(List.of("FaZe Clan", "FaZe Clan", "Team Vitality"));
        when(matchRepository.findByLoserIsNotNull()).thenReturn(List.of("Team Vitality", "G2 Esports", "FaZe Clan"));
        mockChatClientContent("FaZe Clan의 최근 승률은 68% 입니다.");

        // when
        String reply = chatbotService.reply(userId, "FaZe Clan 승률 알려줘");

        // then
        assertThat(reply).isNotNull();
        assertThat(reply).contains("FaZe Clan").contains("승률");
        verify(matchRepository).findByWinnerIsNotNull();
        verify(matchRepository).findByLoserIsNotNull();
    }

    /**
     * ChatClient의 응답 체인을 간소화하여 content()만 mock 처리하는 헬퍼 메서드
     * <p>
     * 호출 흐름: chatClient.prompt() → system() → call() → chatResponse() → getResult() → getOutput() → getText()
     *
     * @param content AI 모델이 생성했다고 가정한 응답 텍스트
     */
    private void mockChatClientContent(String content) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec call = mock(ChatClient.CallResponseSpec.class);
        ChatResponse chatResponse = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage output = mock(AssistantMessage.class);

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(call);
        when(call.chatResponse()).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(output);
        when(output.getText()).thenReturn(content);
    }
}
