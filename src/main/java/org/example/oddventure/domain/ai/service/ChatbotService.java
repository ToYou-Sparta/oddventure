package org.example.oddventure.domain.ai.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
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
                
                [툴 사용 원칙]
                - 일정/승률/예측 등 사실 확인이 필요하면 제공된 툴을 사용한다.
                - 한 번에 여러 툴이 필요하지 않다면 호출을 최소화한다.
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
                .tools(scheduleTools, winRateTools)
                .call();

        String answer = call.content();

        chatHistoryService.addMessage(userId, "user", userMessage);
        chatHistoryService.addMessage(userId, "assistant", answer);

        return answer;
    }
}
