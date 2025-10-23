package org.example.oddventure.domain.ai.integration;

import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.ai.dto.AiRequest;
import org.example.oddventure.domain.ai.dto.AiResponse;
import org.example.oddventure.domain.ai.service.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class WinningRateAiTest {

    @MockitoBean
    private MatchRepository matchRepository;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private AiService aiService;

    @Test
    void AI로_승률_예측을_할_수_있다() {
        //given
        AiRequest request = new AiRequest("팀 T1과 팀 GEN.G의 승률 예측해줘");

        //when
        AiResponse response = aiService.calculateWinningRateWithAi(request);

        //then
        assertThat(response.result()).isTrue();
        assertThat(response.hasTeamName()).isTrue();
        assertThat(response.teamName()).isEqualTo(List.of("T1", "GEN.G"));
        assertThat(response.winningCount()).isEqualTo(List.of(0L, 0L));
        assertThat(response.losingCount()).isEqualTo(List.of(0L, 0L));
        assertThat(response.winningRate()).isEqualTo(List.of(0L, 0L));
    }
}