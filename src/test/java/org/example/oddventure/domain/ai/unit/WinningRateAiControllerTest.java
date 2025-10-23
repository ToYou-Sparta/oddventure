package org.example.oddventure.domain.ai.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.oddventure.base.WithMockAuthUser;
import org.example.oddventure.base.restdocs.RestDocsTestSupport;
import org.example.oddventure.base.restdocs.RestDocsUtils;
import org.example.oddventure.domain.ai.controller.AiController;
import org.example.oddventure.domain.ai.dto.AiRequest;
import org.example.oddventure.domain.ai.dto.AiResponse;
import org.example.oddventure.domain.ai.service.AiService;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@Import({SecurityConfig.class, JwtUtil.class})
@WithMockAuthUser(userId = 1, role = UserRole.ROLE_USER)
public class WinningRateAiControllerTest extends RestDocsTestSupport {

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private AiService aiService;

    @Test
    @DisplayName("AI를 통해 승률을 제공한다")
    void ProvideWinningRate_success() throws Exception {
        // given
        AiRequest request = new AiRequest("팀 T1과 팀 GEN.G의 승률 알려줘");
        AiResponse response = new AiResponse(
                true, true,
                List.of("T1", "GEN.G"),
                List.of(0L, 0L),
                List.of(0L, 0L),
                List.of(0L, 0L),
                """
        팀의 이름은 t1과 GEN.G입니다. 경기 요약이 제공되지 않았으므로
        승리 횟수와 패배 횟수를 계산할 수 없습니다. 따라서 승률도 계산할 수 없습니다.
        """);
        when(aiService.calculateWinningRateWithAi(request)).thenReturn(response);

        // when & then
        ResultActions result = mockMvc.perform(post("/api/v1/ai/winningrate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andDo(restDocs.document(
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.result").description("ai 호출 결과"),
                                fieldWithPath("data.hasTeamName").description("질문 내 팀 이름 존재 여부"),
                                fieldWithPath("data.teamName").description("존재하는 팀 이름"),
                                fieldWithPath("data.winningCount").description("팀 별 승리 카운트"),
                                fieldWithPath("data.losingCount").description("팀 별 패배 카운트"),
                                fieldWithPath("data.winningRate").description("승률"),
                                fieldWithPath("data.content").description("승률 예측 근거"))
                ));
    }
}