package org.example.oddventure.domain.ai.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final static List<String> WINNING_RATE_KEYWORDS = List.of("승률", "이길 확률");
    private final ChatHistoryService chatHistoryService;
    private final ChatClient chatClient;
    private final MatchRepository matchRepository;

    public String reply(Long userId, String userMessage) {
        if (WINNING_RATE_KEYWORDS.stream().anyMatch(userMessage::contains)) {
            return handleWinningRate(userMessage);
        }

        List<String> history = chatHistoryService.getRecentMessages(userId);
        String historyText = String.join("\n", history);

        String prompt = """
                아래는 사용자와 챗봇 간의 최근 대화 내용입니다.
                대화의 흐름을 이해하고, 사용자의 질문에 답변하세요.
                
                대화 내역:
                %s
                
                사용자 요청: %s
                
                응답 지침;
                - 대화의 맥락을 유지하면서 자연스럽게 응답하세요.
                - 경기, 팀 관련 데이터는 사실에 기반해 답변하세요.
                - 불확실한 정보일 경우 추측하지 말고 "정보가 부족합니다."라고 답변하세요.
                - 답변은 한국어로 작성하세요.
                """.formatted(historyText, userMessage);

        ChatResponse chatResponse = chatClient.prompt(prompt)
                .system("너는 e스포츠 데이터 기반 AI 챗봇이다. 사용자와의 맥락을 기억한다.")
                .call()
                .chatResponse();

        Generation result = chatResponse.getResult();
        String aiAnswer = result.getOutput().getText();

        chatHistoryService.addMessage(userId, "user", userMessage);
        chatHistoryService.addMessage(userId, "assistant", aiAnswer);

        return aiAnswer;
    }

    private String handleWinningRate(String userMessage) {
        List<String> winMatches = matchRepository.findByWinnerIsNotNull();
        List<String> loseMatches = matchRepository.findByLoserIsNotNull();

        String summary = buildSummary(winMatches, loseMatches);
        String prompt = """
                아래는 최근 e스포츠 경기 요약 데이터입니다.
                이를 기반으로 사용자의 요청에 답변하세요.
                
                경기 데이터:
                %s
                
                사용자 요청: %s
                
                응답 지침:
                - 팀별 승리/패배 데이터를 바탕으로 합리적인 승률을 계산하세요.
                - 통계적 근거를 간략히 설명하세요.
                - 모든 답변은 자연스러운 한국어 문장으로 작성하세요.
                """.formatted(summary, userMessage);

        ChatResponse chatResponse = chatClient.prompt(prompt)
                .system("너는 e스포츠 경기 데이터 분석 전문가야. 통계적 근거를 들어 승률을 예측해줘.")
                .call()
                .chatResponse();

        Generation result = chatResponse.getResult();
        return result.getOutput().getText();
    }

    // DB에서 가져온 경기 데이터를 팀별로 요약
    public String buildSummary(List<String> winMatches, List<String> loseMatches) {
        Map<String, Long> teamWinningCount = winMatches.stream()
                .collect(Collectors.groupingBy((String winner) -> winner, Collectors.counting()));

        Map<String, Long> teamLosingCount = loseMatches.stream()
                .collect(Collectors.groupingBy((String loser) -> loser, Collectors.counting()));

        StringBuilder sb = new StringBuilder();

        sb.append("팀별 승리 횟수:\n");
        teamWinningCount.forEach(
                (team, count) -> sb.append("- ").append(team).append(": ").append(count).append("번\n"));

        sb.append("\n팀별 패배 횟수:\n");
        teamLosingCount.forEach((team, count) -> sb.append("- ").append(team).append(": ").append(count).append("번\n"));
        return sb.toString();
    }
}
