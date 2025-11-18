package org.example.oddventure.domain.ai.service;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.domain.ai.tools.Cs2NewsTools;
import org.example.oddventure.domain.ai.tools.HotKeywordTools;
import org.example.oddventure.domain.ai.tools.ScheduleTools;
import org.example.oddventure.domain.ai.tools.WinRateTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.tool.annotation.Tool;
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
    private final String system = """
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
                """;

    // LLM 기본 응답
    @Tool(description = "e스포츠 데이터 기반 AI 챗봇")
    public String reply(Long userId, String userMessage) {
        List<String> history = chatHistoryService.getRecentMessages(userId);
        String historyText = String.join("\n", history);

        String prompt = """
                    [최근 대화]
                    %s
                
                    [사용자 요청]
                    %s
                """.formatted(historyText, userMessage);

        CallResponseSpec callTools = chatClient
                .prompt(prompt)
                .system(system)
                .call();

        String answer = callTools.content();
        chatHistoryService.addMessage(userId, "user", userMessage);
        chatHistoryService.addMessage(userId, "assistant", answer);

        return answer;
    }

   // 경기 일정 툴 적용
    public String replySchedule(Long userId, String userMessage) {
        String system = """
                [툴 사용 예시]
                사용자: 오늘 경기 있어요? → query_schedule({"when":"오늘"})
                사용자: 11월 4일 경기 일정 알려줘 → query_schedule_by_date({"month":11,"day":4})
                """;
        return callTools(userId, userMessage, system, scheduleTools);
    }

   // 승률 계산 툴 적용
    public String replyWinRate(Long userId, String userMessage) {
        String system = """
                [툴 사용 예시]
                사용자: Nexus 승률 알려줘 → analyze_winning_rate({"teamA":"Nexus"})
                """;
        return callTools(userId, userMessage, system, winRateTools);
    }

    // 인기 검색어 툴 적용
    public String replyHotKeyword(Long userId, String userMessage) {
        String system = """
                [툴 사용 예시]
                사용자: 요즘 인기 있는 팀은 어디야? → query_hot_keywords()
                """;
        return callTools(userId, userMessage, system, hotKeywordTools);
    }

    // cs2 뉴스 툴 적용
    public String replyCs2News(Long userId, String userMessage) {
        String system = """
                [툴 사용 예시]
                사용자: CS2 최근 소식 알려줘 → query_cs2_news()
                사용자: Counter-Strike 2 패치노트 알려줘 → query_cs2_news()
                """;
        return callTools(userId, userMessage, system, cs2NewsTools);
    }

    // 툴 적용 메서드
    public String callTools(Long userId, String userMessage, String toolSystem, Object... tool) {
        List<String> history = chatHistoryService.getRecentMessages(userId);
        String historyText = String.join("\n", history);

        String prompt = """
                    [최근 대화]
                    %s
                
                    [사용자 요청]
                    %s
                """.formatted(historyText, userMessage);

        CallResponseSpec callTools = chatClient
                .prompt(prompt)
                .system(system+"\n"+toolSystem)
                .tools(tool)
                .call();

        String answer = callTools.content();
        chatHistoryService.addMessage(userId, "user", userMessage);
        chatHistoryService.addMessage(userId, "assistant", answer);

        return answer;
    }

    //tool 분류 메서드
    public String classifyTools(String userMassage) {

        String prompt = """
                너는 사용자 의도를 정확히 분류하는 분류기입니다.
                가능한 출력 값(따옴표 없이):
                - schedule
                - winRate
                - hotKeyword
                - cs2News
                - default
                
                [출력 규칙]
                1. 목록 중 여러 개에 해당할 경우 반점(,)으로 구분해 출력합니다.
                2. 추가 설명, 문장, 마침표, 따옴표, 괄호, 공백 등은 절대 포함하지 않습니다.
                3. 반환값이 없을 경우는 존재하지 않습니다.
                4. 예: schedule
                5. 예: schedule,hotKeyword
                
                [사용자 질문 예시]
                사용자: 오늘 경기 일정 알려줘 → schedule
                사용자: FaZe Clan 팀의 승률 알려줘 → winRate
                사용자: 요즘 제일 핫한 경기 이름 알려줘 → hotKeyword
                사용자: Counter-Strike 2 패치노트 알려줘 → cs2News
                사용자: FaZe Clan 팀의 배당률 알려줘 → default
                사용자: FaZe Clan 팀의 승률과 배당률 알려줘 → winRate,default
                """;

        CallResponseSpec callTools = chatClient
                .prompt(prompt)
                .user(userMassage)
                .call();

        return callTools.content();
    }
}
