package org.example.oddventure.domain.ai.agent;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.EdgeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.example.oddventure.domain.ai.service.ChatbotService;
import org.springframework.stereotype.Service;
import java.util.*;
import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

// llm의 사고 흐름 (workflow, state graph) 구축 service
@Slf4j
@Service
public class AgentExecutor {

    private final ChatbotService chatbotService;

    public AgentExecutor(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    public Map<String, Object> callClassifyAgent(State state) {
        log.info("callClassifyAgent: {}", state);

        var query = (String) state.getInput().get("question");
        Object answer = chatbotService.classifyTools(query);
        Map<String, Object> mid = new HashMap<>();
        mid.put("intent", answer);

        log.info("Classify Agent Output: {}", mid);

        return Map.of(State.MID, mid);
    }

    /**
     * Schedule Agent을 호출해 match에 기반한 경기 일정 제공.
     *
     * @param state workflow의 현재 상태 전달
     * @return output에 경기 일정 답변 포함해 update
     */
    public Map<String, Object> callScheduleAgent(State state) {
        log.info("callScheduleAgent: {}", state);

        var query = (String) state.getInput().get("question");
        Long userId = (Long) state.getInput().get("userId");

        Object answer = chatbotService.replySchedule(userId, query);
        Map<String, Object> output = new HashMap<>();
        output.put("schedule", answer);

        log.info("Schedule Agent Output: {}", output);

        return Map.of(State.OUTPUT, output, State.MID, updateIntentState(state));
    }

    /**
     * WinRate Agent을 호출해 match에 기반한 경기 승률 제공.
     *
     * @param state workflow의 현재 상태 전달
     * @return output에 승률 답변 포함해 update
     */
    public Map<String, Object> callWinRateAgent(State state) {
        log.info("callWinRateAgent: {}", state);

        var query = (String) state.getInput().get("question");
        Long userId = (Long) state.getInput().get("userId");

        Object answer = chatbotService.replyWinRate(userId, query);
        Map<String, Object> output = new HashMap<>();
        output.put("winRate", answer);

        log.info("WinRate Agent Output: {}", output);

        return Map.of(State.OUTPUT, output, State.MID, updateIntentState(state));
    }

    /**
     * HotKeyword Agent을 호출해 match에 기반한 경기 인기검색어 제공.
     *
     * @param state workflow의 현재 상태 전달
     * @return output에 승률 답변 포함해 update
     */
    public Map<String, Object> callHotKeywordAgent(State state) {
        log.info("callHotKeywordAgent: {}", state);

        var query = (String) state.getInput().get("question");
        Long userId = (Long) state.getInput().get("userId");

        Object answer = chatbotService.replyHotKeyword(userId, query);
        Map<String, Object> output = new HashMap<>();
        output.put("hotKeyword", answer);

        log.info("HotKeyword Agent Output: {}", output);

        return Map.of(State.OUTPUT, output, State.MID, updateIntentState(state));
    }

    /**
     * CS2News Agent을 호출해 match에 기반한 경기 뉴스 제공.
     *
     * @param state workflow의 현재 상태 전달
     * @return output에 승률 답변 포함해 update
     */
    public Map<String, Object> callCs2NewsAgent(State state) {
        log.info("callCS2NewsAgent: {}", state);

        var query = (String) state.getInput().get("question");
        Long userId = (Long) state.getInput().get("userId");

        Object answer = chatbotService.replyCs2News(userId, query);
        Map<String, Object> output = new HashMap<>();
        output.put("cs2News", answer);

        log.info("CS2News Agent Output: {}", output);

        return Map.of(State.OUTPUT, output, State.MID, updateIntentState(state));
    }

    public Map<String, Object> callDefaultAgent(State state) {
        log.info("callDefaultAgent: {}", state);

        var query = (String) state.getInput().get("question");
        Long userId = (Long) state.getInput().get("userId");

        Object answer = chatbotService.reply(userId, query);
        Map<String, Object> output = new HashMap<>();
        output.put("default", answer);

        log.info("default Agent Output: {}", output);

        return Map.of(State.OUTPUT, output, State.MID, updateIntentState(state));
    }

    /**
     * workflow graph Builder 제공
     *
     * @return GraphBuilder 객체 반환
     */
    public GraphBuilder graphBuilder() {
        return new GraphBuilder();
    }

    /**
     * workflow의 상태
     * (입력, 중간, 출력)
     */
    public static class State extends AgentState {
        public static final String INPUT = "input";
        public static final String MID = "mid";
        public static final String OUTPUT = "output";

        static Map<String, Channel<?>> SCHEMA = Map.of(
                INPUT, Channels.<Map<String, Object>>base(() -> new HashMap<>()),
                OUTPUT, Channels.<Map<String, Object>>base(() -> new HashMap<>()),
                MID, Channels.<Map<String, Object>>base(() -> new HashMap<>())
        );

        /**
         * input, output data 주입을 통해 state 구축
         * input = 사용자 질문
         * output = flow를 거친 후 반환하는 최종 답변
         *
         * @param initData state 데이터 초기화
         */
        public State(Map<String, Object> initData) {
            super(initData);
        }

        public Map<String, Object> getInput() {
            return this.<Map<String, Object>>value(INPUT).orElseGet(HashMap::new);
        }

        public Map<String, Object> getMid() {
            return this.<Map<String, Object>>value(MID).orElseGet(HashMap::new);
        }

        public Map<String, Object> getOutput() {
            return this.<Map<String, Object>>value(OUTPUT).orElseGet(HashMap::new);
        }
    }

    //agent workflow를 위한 StateGraph Builder 클래스
    public class GraphBuilder {
        /**
         * 노드 정의와 실행을 위한 workflow graph 구축
         *
         * @return 생성된 StateGraph 반환
         * @throws GraphStateException If 그래프 미생성 시 예외
         */
        public StateGraph<State> build() throws GraphStateException {
            var shouldContinue = (EdgeAction<State>) state -> {
                log.info("shouldContinue state: {}", state);

                String intent = (String) state.getMid().get("intent");
                if (intent.isBlank()) {
                    return "end"; // flow 실행 완료 후 END로 연결, 체이닝 종료
                }

                List<String> intentList = new ArrayList<>(Arrays.stream(intent.split(",")).toList());
                if (intentList.isEmpty()) {
                    return "end"; // flow 실행 완료 후 END로 연결, 체이닝 종료
                }

                // intent 내에 툴 중 맨 앞의 agent 추출 후 case 분기 및 실행
                String current = intentList.get(0);
                return switch (current) {
                    case "schedule" -> "scheduleAgent";
                    case "winRate" -> "winRateAgent";
                    case "hotKeyword" -> "hotKeywordAgent";
                    case "cs2News" -> "cs2NewsAgent";
                    case "default" -> "defaultAgent";
                    default -> throw new IllegalStateException("Unexpected value: " + current);
                };
            };

            Map<String, String> toolList = Map.of(
                    "scheduleAgent", "scheduleAgent",
                    "winRateAgent", "winRateAgent",
                    "hotKeywordAgent", "hotKeywordAgent",
                    "cs2NewsAgent", "cs2NewsAgent",
                    "defaultAgent", "defaultAgent",
                    "end", END
            );

            return new StateGraph<>(State.SCHEMA, State::new)
                    .addEdge(START, "classifyAgent") // classifyAgent 맨 처음 실행, llm을 통한 tool 판단
                    .addNode("classifyAgent", node_async(AgentExecutor.this::callClassifyAgent)) // 노드 추가
                    .addConditionalEdges("classifyAgent",
                            edge_async(shouldContinue), toolList) // 분기 결과에 맞는 노드 연결
                    .addNode("scheduleAgent", node_async(AgentExecutor.this::callScheduleAgent))
                    .addConditionalEdges("scheduleAgent",
                            edge_async(shouldContinue), toolList)
                    .addNode("winRateAgent", node_async(AgentExecutor.this::callWinRateAgent))
                    .addConditionalEdges("winRateAgent",
                            edge_async(shouldContinue), toolList)
                    .addNode("hotKeywordAgent", node_async(AgentExecutor.this::callHotKeywordAgent))
                    .addConditionalEdges("hotKeywordAgent",
                            edge_async(shouldContinue), toolList)
                    .addNode("cs2NewsAgent", node_async(AgentExecutor.this::callCs2NewsAgent))
                    .addConditionalEdges("cs2NewsAgent",
                            edge_async(shouldContinue), toolList)
                    .addNode("defaultAgent", node_async(AgentExecutor.this::callDefaultAgent))
                    .addConditionalEdges("defaultAgent",
                            edge_async(shouldContinue), toolList)
                    .addNode("end", node_async(state -> {
                        log.info("output: {}", State.OUTPUT);
                        return Map.of("output", state.getOutput());}))
                    .addEdge("end", END);
        }
    }

    // agent 호출 완료 시 mid state에서 제거, mid 상태 업데이트
    public Map<String, Object> updateIntentState(State state) {
        String intent = (String) state.getMid().get("intent");
        List<String> intentList = new ArrayList<>(Arrays.stream(intent.split(",")).toList());
        intentList.remove(0);
        Map<String, Object> mid = state.getMid();
        mid.put("intent", String.join(",",intentList));

        return mid;
    }
}
