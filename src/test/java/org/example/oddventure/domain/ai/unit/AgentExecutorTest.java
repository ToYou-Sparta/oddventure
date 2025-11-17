package org.example.oddventure.domain.ai.unit;

import org.bsc.langgraph4j.GraphStateException;
import org.example.oddventure.domain.ai.agent.AgentExecutor;
import org.example.oddventure.domain.ai.service.ChatbotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AgentExecutorTest {

    @Mock
    private ChatbotService chatbotService;

    @InjectMocks
    private AgentExecutor agentExecutor;

    @Test
    @DisplayName("schedule agent를 호출한다.")
    public void testScheduleAgent(){
        // given
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> mid = new HashMap<>();
        Map<String, Object> initData = new HashMap<>();
        input.put("question","오늘은 어떤 경기가 있나요?");
        mid.put("intent", List.of("schedule"));
        initData.put(AgentExecutor.State.INPUT, input);
        initData.put(AgentExecutor.State.MID, mid);
        AgentExecutor.State state = new AgentExecutor.State(initData);
        when(chatbotService.replySchedule(any(), any()))
                .thenReturn("안녕하세요! 오늘은 FaZe Clan과 Team Vitality의 경기가 있어요.");

        // when
        Map<String, Object> result = agentExecutor.callScheduleAgent(state);

        // then
        assertThat(((Map<String, Object>) result.get(AgentExecutor.State.OUTPUT)).get("schedule"))
                .isEqualTo("안녕하세요! 오늘은 FaZe Clan과 Team Vitality의 경기가 있어요.");
    }

    @Test
    @DisplayName("winRate agent를 호출한다.")
    public void testWinRateAgent(){
        // given
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> mid = new HashMap<>();
        Map<String, Object> initData = new HashMap<>();
        input.put("question","FaZe Clan 팀의 승률 알려줘.");
        mid.put("intent", List.of("winRate"));
        initData.put(AgentExecutor.State.INPUT, input);
        initData.put(AgentExecutor.State.MID, mid);
        AgentExecutor.State state = new AgentExecutor.State(initData);
        when(chatbotService.replyWinRate(any(), any()))
                .thenReturn("FaZe Clan과 Team Vitality의 승률은 각각 70%, 30% 입니다.");

        // when
        Map<String, Object> result = agentExecutor.callWinRateAgent(state);

        // then
        assertThat(((Map<String, Object>) result.get(AgentExecutor.State.OUTPUT)).get("winRate"))
                .isEqualTo("FaZe Clan과 Team Vitality의 승률은 각각 70%, 30% 입니다.");
    }

    @Test
    @DisplayName("hotKeyword agent를 호출한다.")
    public void testHotKeywordAgent(){
        // given
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> mid = new HashMap<>();
        Map<String, Object> initData = new HashMap<>();
        input.put("question","요즘 인기 있는 팀은 어디야?");
        mid.put("intent", List.of("hotKeyword"));
        initData.put(AgentExecutor.State.INPUT, input);
        initData.put(AgentExecutor.State.MID, mid);
        AgentExecutor.State state = new AgentExecutor.State(initData);
        when(chatbotService.replyHotKeyword(any(), any()))
                .thenReturn("요즘은 FaZe Clan 팀의 경기가 인기있어요.");

        // when
        Map<String, Object> result = agentExecutor.callHotKeywordAgent(state);

        // then
        assertThat(((Map<String, Object>) result.get(AgentExecutor.State.OUTPUT)).get("hotKeyword"))
                .isEqualTo("요즘은 FaZe Clan 팀의 경기가 인기있어요.");
    }

    @Test
    @DisplayName("cs2News agent를 호출한다.")
    public void testCs2NewsAgent(){
        // given
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> mid = new HashMap<>();
        Map<String, Object> initData = new HashMap<>();
        input.put("question","CS2 최근 소식 알려줘.");
        mid.put("intent", List.of("cs2News"));
        initData.put(AgentExecutor.State.INPUT, input);
        initData.put(AgentExecutor.State.MID, mid);
        AgentExecutor.State state = new AgentExecutor.State(initData);
        when(chatbotService.replyCs2News(any(), any()))
                .thenReturn("이제 StarLadder 부다페스트 메이저 대회 허브를 이용할 수 있습니다. " +
                        "허브를 방문하여 토너먼트 아이템을 구매하고 승자 예측 도전을 플레이하는 등 다양한 콘텐츠를 즐겨보세요.");

        // when
        Map<String, Object> result = agentExecutor.callCs2NewsAgent(state);

        // then
        assertThat(((Map<String, Object>) result.get(AgentExecutor.State.OUTPUT)).get("cs2News"))
                .isEqualTo("이제 StarLadder 부다페스트 메이저 대회 허브를 이용할 수 있습니다. " +
                        "허브를 방문하여 토너먼트 아이템을 구매하고 승자 예측 도전을 플레이하는 등 다양한 콘텐츠를 즐겨보세요.");
    }

    @Test
    @DisplayName("default agent를 호출한다.")
    public void testDefaultAgent(){
        // given
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> mid = new HashMap<>();
        Map<String, Object> initData = new HashMap<>();
        input.put("question","오늘 날씨 어때?");
        mid.put("intent", List.of("default"));
        initData.put(AgentExecutor.State.INPUT, input);
        initData.put(AgentExecutor.State.MID, mid);
        AgentExecutor.State state = new AgentExecutor.State(initData);
        when(chatbotService.reply(any(), any()))
                .thenReturn("해당 정보가 부족하여 답변드릴 수 없습니다. e-sports 관련한 질문에 관해 답변해드릴게요!");

        // when
        Map<String, Object> result = agentExecutor.callDefaultAgent(state);

        // then
        assertThat(((Map<String, Object>) result.get(AgentExecutor.State.OUTPUT)).get("default"))
                .isEqualTo("해당 정보가 부족하여 답변드릴 수 없습니다. e-sports 관련한 질문에 관해 답변해드릴게요!");
    }

    @Test
    @DisplayName("한 번에 여러 agent를 호출한다.")
    public void testMultiAgent() throws GraphStateException {
        // given
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> initData = new HashMap<>();
        input.put("question", "요즘 인기 있는 팀의 승률 알려줘.");
        input.put("userId", 1);
        initData.put(AgentExecutor.State.INPUT, input);
        AgentExecutor.State state = new AgentExecutor.State(initData);


        when(chatbotService.replyHotKeyword(any(), any()))
                .thenReturn("요즘 인기있는 팀은 FaZe Clan 입니다.");
        when(chatbotService.replyWinRate(any(), any()))
                .thenReturn("FaZe Clan의 승률은 70% 입니다.");
        when(chatbotService.classifyTools(any()))
                .thenReturn(List.of("hotKeyword, winRate"));

        var graph = agentExecutor.graphBuilder().build();
        var app = graph.compile();

        // when
        Optional<AgentExecutor.State> result = app.invoke(initData);

        // then
        assertThat(((Map<String, Object>) result.get()).get("hotKeyword"))
                .isEqualTo("요즘 인기있는 팀은 FaZe Clan 입니다.");
        assertThat(((Map<String, Object>) result.get()).get("winRate"))
                .isEqualTo("FaZe Clan의 승률은 70% 입니다.");
    }
}
