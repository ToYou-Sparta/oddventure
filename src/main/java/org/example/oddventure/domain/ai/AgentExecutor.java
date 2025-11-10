package org.example.oddventure.domain.ai;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.EdgeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.example.oddventure.domain.ai.service.AgentService;
import org.example.oddventure.domain.ai.service.ChatbotService;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * Service responsible for executing agent workflows. This includes handling match, time,
 * and winning_rate recommendations based on user inputs.
 */
@Slf4j
@Service
public class AgentExecutor {

    private final AgentService agentService;
    private final ChatbotService chatbotService;

    public AgentExecutor(AgentService agentService, ChatbotService chatbotService) {
        this.agentService = agentService;
        this.chatbotService = chatbotService;
    }

    /**
     * Calls the Betting Agent to retrieve betting information based on user input.
     *
     * @param state The current state of the workflow.
     * @return A map containing the betting details as output.
     */
    Map<String, Object> callMatchAgent(State state) {
        log.info("callMatchAgent: {}", state);
        log.info("Match Agent Input: {}", state.getInput());

        var query = (String) state.getInput().get("query");
        var response = agentService.execute(query, List.of());

        Map<String, Object> output = new HashMap<>();
        output.put("match", response);
        log.info("Match Agent Output: {}", output);

        return Map.of(State.OUTPUT, output);
    }

    /**
     * Time Agent을 호출해 match에 기반한 경기 시간 제공.
     *
     * @param state workflow의 현재 상태 전달
     * @return output에 경기 시간 답변 포함해 update
     */
    Map<String, Object> callTimeAgent(State state) {
        log.info("callTimeAgent: {}", state);

        var match = (String) state.getOutput().get("match");

        Object answer = chatbotService.reply(null, match); //사용자 id 임시로 null

        Map<String, Object> output = new HashMap<>();
        output.put("time", answer);
        log.info("Time Agent Output: {}", output);

        return Map.of(State.MID, output);
    }

    /**
     * Calls the Food Agent to provide food suggestions based on travel recommendations.
     *
     * @param state workflow의 현재 상태 전달
     * @return output에 승률 답변 포함해 update
     */
    Map<String, Object> callWinningRateAgent(State state) {
        log.info("callWinningRateAgent: {}", state);

        var time = (String) state.getMID().get("time");

        Object answer = chatbotService.reply(null, time); //사용자 id 임시로 null

        Map<String, Object> output = new HashMap<>();
        output.put("winningRate", answer);
        log.info("winningRate Agent Output: {}", output);

        return Map.of(State.WINNING_RATE, output);
    }

    /**
     * Provides a builder to construct the workflow graph.
     *
     * @return An instance of GraphBuilder.
     */
    public GraphBuilder graphBuilder() {
        return new GraphBuilder();
    }

    /**
     * Represents the state of the workflow, including input, intermediate, and output data.
     */
    public static class State extends AgentState {
        public static final String INPUT = "question";
        public static final String MID = "matchInformation";
        public static final String OUTPUT = "match";
        public static final String WINNING_RATE = "winningRate";

        static Map<String, Channel<?>> SCHEMA = Map.of(
                INPUT, Channels.base(() -> new HashMap<>()),
                OUTPUT, Channels.base(() -> new HashMap<>()),
                MID, Channels.base(() -> new HashMap<>())
        );

        /**
         * Constructor to initialize state with given data.
         *
         * @param initData Initial data for the state.
         */
        public State(Map<String, Object> initData) {
            super(initData);
        }

        public Map<String, Object> getInput() {
            return this.<Map<String, Object>>value(INPUT).orElseGet(HashMap::new);
        }

        public Map<String, Object> getOutput() {
            return this.<Map<String, Object>>value(OUTPUT).orElseGet(HashMap::new);
        }

        public Map<String, Object> getMID() {
            return this.<Map<String, Object>>value(MID).orElseGet(HashMap::new);
        }
    }

    /**
     * Builder class to construct a StateGraph for the agent workflow.
     */
    public class GraphBuilder {

        /**
         * Builds the workflow graph by defining nodes and transitions.
         *
         * @return The constructed StateGraph.
         * @throws GraphStateException If the graph cannot be constructed.
         */
        public StateGraph<State> build() throws GraphStateException {
            var shouldContinue = (EdgeAction<State>) state -> {
                log.info("shouldContinue state: {}", state);
                return state.getInput().containsKey("time") ? "timeAgent" : "end";
            };

            return new StateGraph<>(State.SCHEMA, State::new)
                    .addEdge(START, "matchAgent")
                    .addNode("matchAgent", node_async(AgentExecutor.this::callMatchAgent))
                    .addConditionalEdges("matchAgent",
                            edge_async(shouldContinue),
                            Map.of(
                                    "timeAgent", "timeAgent",
                                    "end", END
                            )
                    )
                    .addNode("timeAgent", node_async(AgentExecutor.this::callTimeAgent))
                    .addEdge("timeAgent", "winningRateAgent")
                    .addNode("winningRateAgent", node_async(AgentExecutor.this::callWinningRateAgent))
                    .addEdge("winningRateAgent", END);
        }
    }
}
