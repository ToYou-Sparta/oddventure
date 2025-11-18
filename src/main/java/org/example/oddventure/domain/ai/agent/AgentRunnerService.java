package org.example.oddventure.domain.ai.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

/**
 * 멀티 에이전트 AI 애플리케이션 실행 service
 * agent workflow 초기화, 사용자 쿼리 처리, 구성된 상태 그래프를 기반으로 결과 생성
 */
@Service
@RequiredArgsConstructor
public class AgentRunnerService {

    private final AgentExecutor agentExecutor;

    public Optional<AgentExecutor.State> execute(Long userId, String question) throws Exception {

        // AgentExecutor를 이용해 Graph 생성
        var graph = agentExecutor.graphBuilder().build();
        var app = graph.compile();

        // 데이터 주입을 통한 workflow 초기화
        var inputData = Map.<String, Object>of(
                AgentExecutor.State.INPUT, Map.of("question", question, "userId", userId)
        );

        // 워크플로를 실행 및 최종 출력 기록
        return app.invoke(inputData);
    }
}
