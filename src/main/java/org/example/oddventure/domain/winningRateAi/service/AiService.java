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

    public AiResponse generateAbnormalBehaviorReport(AiRequest request) {
        List<Match> winMatches = matchRepository.findByWinnerIsNotNull();
        List<Match> loseMatches = matchRepository.findByLoserIsNotNull();

        // 데이터 요약 문자열 생성
        String summary = buildSummary(winMatches, loseMatches);
        String prompt = generatePrompt(request);

        // Groq 프롬프트 생성
        ChatResponse chatResponse = this.chatClient.prompt(prompt)
                .system("경기 요약:\n"+summary)
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
                Task: Extract teamName from the text and return a JSON response.
               
                - Identify teamName and find the team at summary and extract winningCount,losingCount.
                - Based on the game summary provided below, calculate each team's winning percentage
                  and explain the exact analytical factors for the conclusion becomes `content`.
                - If no teamName is found, return:
                  {"result": true, "hasTeamName": false}
                - In korean.
               
                Respond in JSON format only, with the following fields:
                - result
                - hasTeamName
                - teamName (array of strings)
                - winningCount (array of longs)
                - losingCount (array of longs)
                - winningRate (array of longs)
                - content
               
                ===
               
                input:
                {
                    "content": "%s"
                }
               """, request.content());
    }

    private AiResponse parseResult(String text) {
        String jsonText = text.lines()
                .filter(line -> !line.startsWith("```"))
                .reduce("", (a, b) -> a + b);
        try {
            return objectMapper.readValue(jsonText, AiResponse.class); //역직렬화
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}