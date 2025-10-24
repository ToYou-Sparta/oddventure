package org.example.oddventure.domain.ai.unit;

import org.example.oddventure.domain.ai.dto.AiRequest;
import org.example.oddventure.domain.ai.dto.AiResponse;
import org.example.oddventure.domain.ai.service.AiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WinningRateAiServiceTest {

    @InjectMocks
    private AiService aiService;

    @Test
    @DisplayName("팀 별 승패를 요약 문자열로 생성할 수 있다")
    void createRecordSummary() {
        // given
        List<String> winMatches = List.of("T1", "GEN.G", "T1", "DRX");
        List<String> loseMatches = List.of("GEN.G", "T1", "DRX", "DRX");

        // when
        String summary = aiService.buildSummary(winMatches, loseMatches);

        // then
        assertThat(summary).isEqualTo("""
        팀별 승리 횟수:
        - GEN.G: 1번
        - DRX: 1번
        - T1: 2번

        팀별 패배 횟수:
        - GEN.G: 1번
        - DRX: 2번
        - T1: 1번
        """);
    }

    @Test
    @DisplayName("기본 프롬프트를 생성할 수 있다")
    void createGeneratePrompt() {
        // given
        AiRequest request = new AiRequest("팀 T1과 팀 GEN.G의 승률 예측해줘");
        // when
        String summary = aiService.generatePrompt(request);

        // then
        assertThat(summary).contains("""
         input:
         {
             "content": "팀 T1과 팀 GEN.G의 승률 예측해줘"
         }
        """);
    }

    @Test
    @DisplayName("JSON 문자열을 AiResponse로 파싱할 수 있다")
    void parsingJsonToAiResponse () {
        // given
        String jsonText = """
                {
                  "result": true,
                  "hasTeamName": true,
                  "teamName": ["T1", "GEN.G"],
                  "winningCount": [1, 0],
                  "losingCount": [0, 1],
                  "winningRate": [100, 0]
                }
                """;

        // when
        AiResponse response = aiService.parseResult(jsonText);

        // then
        assertThat(response.result()).isTrue();
        assertThat(response.hasTeamName()).isTrue();
        assertThat(response.teamName()).isEqualTo(List.of("T1", "GEN.G"));
        assertThat(response.winningCount()).isEqualTo(List.of(1L, 0L));
        assertThat(response.losingCount()).isEqualTo(List.of(0L, 1L));
        assertThat(response.winningRate()).isEqualTo(List.of(100L, 0L));
    }
}
