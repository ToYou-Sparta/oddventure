package org.example.oddventure.domain.winningRateAi.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.match.entity.Match;
import org.example.oddventure.domain.match.repository.MatchRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Service
@RequiredArgsConstructor
public class AiService {
    /**
     * 경기 전적 데이터 레파지토리에서 가져와 ai한테 전달
     **/

    private final MatchRepository matchRepository;
    private final ChatModel chatModel;
    private final ChatClient chatClient;

    public String generateAbnormalBehaviorReport(String userInput) {
        List<Match> matches = matchRepository.findAllByWinner();
        String startDate = "2025-07-07";
        String endDate = "2025-07-14";

        // 데이터 요약 문자열 생성
        String summary = buildSummary(matches);

        // Groq 프롬프트 생성
        String prompt = this.chatClient.prompt()
                .system("한국어로 대답하고 네가 낸 결론에 대한 정확한 분석 요인을 나열해.")
                .system(summary)
                .user(userInput)
                .call()
                .content();

        try {
            return chatModel.call(prompt);
        } catch (Exception e) {
            // 오류 로깅
            System.err.println("Groq API 호출 중 오류 발생: " + e.getMessage());
            // 대체 메시지 반환 또는 사용자 지정 예외 다시 던지기
            return "답변 생성 중 오류가 발생했습니다.";
        }
    }

    private String buildSummary(List<Match> matches) {
        Map<String, Long> teamGameCount = matches.stream()
                .collect(Collectors.groupingBy(Match::getTeamA, Collectors.counting())); //논의 필요

        Map<String, Long> teamWinningCount = matches.stream()
                .collect(Collectors.groupingBy(Match::getTeamB, Collectors.counting()));

        StringBuilder sb = new StringBuilder();

        sb.append("팀별 총 경기 횟수:\n");
        teamGameCount.forEach((team, count) -> sb.append("- ").append(team).append(": ").append(count).append("번\n"));

        sb.append("\n팀별 승패 횟수:\n");
        teamWinningCount.forEach((team, count) -> sb.append("- ").append(team).append(": ").append(count).append("번\n"));

        return sb.toString();
    }
}