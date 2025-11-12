package org.example.oddventure.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.ai.tools.Cs2NewsTools;
import org.example.oddventure.domain.ai.tools.HotKeywordTools;
import org.example.oddventure.domain.ai.tools.ScheduleTools;
import org.example.oddventure.domain.ai.tools.WinRateTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatHistoryService chatHistoryService;
    private final ChatClient chatClient;
    private final ScheduleTools scheduleTools;
    private final WinRateTools winRateTools;
    private final HotKeywordTools hotKeywordTools;
    private final Cs2NewsTools cs2NewsTools;

    public String reply(Long userId, String userMessage) {
        List<String> history = chatHistoryService.getRecentMessages(userId);
        String historyText = String.join("\n", history);

        String system = """
                너는 e스포츠 도메인 어시스턴트다.
                
                [사고 지침(내부)]
                - 질문을 1) 의도 파악 → 2) 필요 데이터 확인 → 3) 툴 선택/인자 결정 → 4) 결과 통합 → 5) 검증 체크리스트 순으로 생각한다.
                - 필요한 경우에만 툴을 호출한다. 툴 결과의 범위/단위/시간대를 점검한다.
                - 모순되면 재검토하고, 데이터가 부족하면 부족하다고 말한다.
                
                [출력 지침(외부)]
                - 최종 한국어 답변만 출력한다. 중간 사고 과정, 단계적 이유, 체크리스트는 출력하지 않는다.
                - 간결하지만, 사용자에게 실용적인 요약/근거(출처 유형)를 포함한다.
                - 목록형보다는 자연스러운 문장으로 응답한다.
                - 가능하면 출처(URL)나 공식 링크를 마지막에 함께 안내한다.
                - 불필요한 반복 표현은 피하고, 핵심 내용 위주로 간결히 요약한다.
                
                [툴 사용 원칙]
                - 경기 일정이 필요하면 query_schedule 또는 query_schedule_by_date를 사용한다.
                - 팀·리그의 승률이 필요하면 analyze_winning_rate를 사용한다.
                - 최근 인기 있는 팀/리그를 묻는다면 query_hot_keywords를 사용한다.
                - Counter-Strike 2의 최근 뉴스·패치·이벤트를 묻는다면 query_cs2_news를 사용한다.
                - 한 번에 여러 툴이 필요하지 않다면 호출을 최소화한다.
                
                [툴 사용 예시]
                사용자: 안녕하세요! → 툴 호출 없이 최종 답만 생성
                사용자: 오늘 경기 있어요? → query_schedule({"when":"오늘"})
                사용자: 11월 4일 경기 일정 알려줘 → query_schedule_by_date({"month":11,"day":4})
                사용자: Nexus 승률 알려줘 → analyze_winning_rate({"teamA":"Nexus"})
                사용자: 요즘 인기 있는 팀은 어디야? → query_hot_keywords()
                사용자: CS2 최근 소식 알려줘 → query_cs2_news()
                사용자: Counter-Strike 2 패치노트 알려줘 → query_cs2_news()
                """;

        String prompt = """
                    [최근 대화]
                    %s
                
                    [사용자 요청]
                    %s
                """.formatted(historyText, userMessage);

        CallResponseSpec call = chatClient
                .prompt(prompt)
                .system(system)
                .tools(scheduleTools, winRateTools, hotKeywordTools, cs2NewsTools)
                .call();

        String answer = call.content();

        chatHistoryService.addMessage(userId, "user", userMessage);
        chatHistoryService.addMessage(userId, "assistant", answer);

        return answer;
    }
}
