package org.example.oddventure.domain.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;

/**
 * Entry point for the Multi-Agent AI Application. This application initializes the agent workflow,
 * processes user queries, and generates results based on the configured state graph.
 */
@Service
@RequiredArgsConstructor
public class AgentRunnerService {

    private final AgentExecutor agentExecutor;

    public Optional<AgentExecutor.State> execute(String question) throws Exception {

        // AgentExecutor를 이용해 state graph 생성
        var graph = agentExecutor.graphBuilder().build();
        var app = graph.compile();

        // Input data to initialize the workflow
        var inputData = Map.<String, Object>of(
                AgentExecutor.State.INPUT, Map.of("question", question)
        );

        // Execute the workflow and log the final output
        return app.invoke(inputData);
    }
}
