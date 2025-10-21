package org.example.oddventure.domain.winningRateAi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.example.oddventure.domain.winningRateAi.dto.AiRequest;
import org.example.oddventure.domain.winningRateAi.dto.AiResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Service
@RequiredArgsConstructor
public class AiService {

    private final MatchRepository matchRepository;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiResponse generateAbnormalBehaviorReport(String userInput) {
        List<Match> winMatches = matchRepository.findByWinner();
        List<Match> loseMatches = matchRepository.findByLoser();

        // 데이터 요약 문자열 생성
        String summary = buildSummary(winMatches, loseMatches);

        // Groq 프롬프트 생성
        ChatResponse chatResponse = this.chatClient.prompt()
                .system("""
                        아래 제공된 경기 요약(summary)을 기반으로
                        각 팀의 승률을 계산하고, 결론에 대한 정확한 분석 요인을 설명해.
                        모든 대답은 한국어로 작성해.""")
                .system("경기 요약:\n"+summary)
                .user(userInput)
                .call()
                .chatResponse();

        if (chatResponse == null) {
            throw new RuntimeException("Chat response is null");
        }
        Generation result = chatResponse.getResult();

        AssistantMessage output = result.getOutput();
        String text = output.getText();

        try {
            return parseResult(text);
        } catch (Exception e) {
            // 오류 로깅
            System.err.println("Groq API 호출 중 오류 발생: " + e.getMessage());
            // 대체 메시지 반환 또는 사용자 지정 예외 다시 던지기
            throw new RuntimeException(e);
        }
    }

    private String buildSummary(List<Match> winMatches, List<Match> loseMatches) {

        Map<String, Long> teamWinningCount = winMatches.stream()
                .collect(Collectors.groupingBy(Match::getWinner, Collectors.counting()));

        Map<String, Long> teamlosingCount = loseMatches.stream()
                .collect(Collectors.groupingBy(Match::getLoser, Collectors.counting()));


        StringBuilder sb = new StringBuilder();

        sb.append("팀별 승리 횟수:\n");
        teamWinningCount.forEach((team, count) -> sb.append("- ").append(team).append(": ").append(count).append("번\n"));

        sb.append("\n팀별 패배 횟수:\n");
        teamlosingCount.forEach((team, count) -> sb.append("- ").append(team).append(": ").append(count).append("번\n"));
        return sb.toString();
    }

    private String generatePrompt(AiRequest request) {
        return String.format("""
                Task: Extract team name from the text and return a JSON response.
               
                - Identify team name and find the team at summary.
                - Remove the identified team name from the text. The remaining text becomes `content`.
                - If no team name is found, return:
                  {"result": true, "hasTeamName": false}
                - If team name time exists, return:
                  {"result": false }
               
                Respond in JSON format only, with the following fields:
                - result
                - hasTeamName
                - teamName
                - winningCount
                - losingCount
                - winningRate
                - content
               
                ===
               
                input:
                {
                    "teamName": "%s",
                    "content": "%s"
                }
               """, request.teamName(), request.content());
    }

    private AiResponse parseResult(String text) {
        String jsonText = text.lines()
                .filter(line -> !line.startsWith("```"))
                .reduce("", (a, b) -> a + b);
        try {
            return objectMapper.readValue(jsonText, AiResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}