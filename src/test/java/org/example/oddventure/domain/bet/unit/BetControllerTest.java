package org.example.oddventure.domain.bet.unit;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.example.oddventure.base.WithMockAuthUser;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.bet.controller.BetController;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.dto.response.BetCreateResponse;
import org.example.oddventure.domain.bet.dto.response.BetDeleteResponse;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.service.BetService;
import org.example.oddventure.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

@WebMvcTest(BetController.class)
@ExtendWith(RestDocumentationExtension.class)
@Import({SecurityConfig.class, JwtUtil.class})
@WithMockAuthUser(userId = 1, role = UserRole.ROLE_USER)
public class BetControllerTest {

    @MockitoBean
    private BetService betService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private RestDocumentationResultHandler restDocs;

    @BeforeEach
    void setup(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.restDocs = MockMvcRestDocumentation.document("{class-name}/{method-name}",
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new CharacterEncodingFilter(StandardCharsets.UTF_8.name(), true))
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .alwaysDo(restDocs) // 디폴트 설정
                .build();
    }

    @Test
    @DisplayName("POST /bets - 베팅 생성 성공")
    public void createBet_success() throws Exception {
        //given
        Long userId = 1L;

        Long betId = 1L;
        Long matchId = 1L;
        BetCreateRequest request = new BetCreateRequest(matchId, SelectedTeam.Team_A, 1000L);
        BetCreateResponse response = BetCreateResponse.builder()
                .betId(betId)
                .userId(userId)
                .selectedTeam(SelectedTeam.Team_A)
                .selectedTeamName("T1")
                .betAmount(BigDecimal.valueOf(1000))
                .oddsAtBetting(BigDecimal.valueOf(1.50))
                .userPointAfter(BigDecimal.valueOf(0))
                .build();

        when(betService.createBet(userId, request)).thenReturn(response);

        //when & then
        mockMvc.perform(post("/api/v1/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("matchId").description("경기 ID"),
                                fieldWithPath("selectedTeam").description("선택한 팀"),
                                fieldWithPath("betAmount").description("베팅 금액")
                        ),
                        responseFields(
                                fieldWithPath("httpStatus").description("HTTP 상태 코드"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data").description("응답 데이터"),
                                fieldWithPath("data.betId").description("베팅 ID"),
                                fieldWithPath("data.userId").description("사용자 ID"),
                                fieldWithPath("data.selectedTeam").description("선택한 팀"),
                                fieldWithPath("data.selectedTeamName").description("선택한 팀 이름"),
                                fieldWithPath("data.betAmount").description("베팅 금액"),
                                fieldWithPath("data.oddsAtBetting").description("베팅 시점 배당률"),
                                fieldWithPath("data.userPointAfter").description("사용자 포인트 잔액"),
                                fieldWithPath("timestamp").description("응답 시간")
                        )
                ));
    }

    @Test
    @DisplayName("DELETE /bets/{betId} - 베팅 취소 성공")
    public void deleteBet_success() throws Exception {
        //given
        Long userId = 1L;
        Long betId = 1L;

        BetDeleteResponse response = BetDeleteResponse.builder()
                .betId(betId)
                .refundAmount(BigDecimal.valueOf(1000))
                .userPointAfter(BigDecimal.ZERO)
                .build();

        when(betService.deleteBet(userId, betId)).thenReturn(response);

        //when & then
        mockMvc.perform(delete("/api/v1/bets/{betId}", betId))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("betId").description("베팅 ID")
                        ),
                        responseFields(
                                fieldWithPath("httpStatus").description("HTTP 상태 코드"),
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("message").description("응답 메시지"),
                                fieldWithPath("data").description("응답 데이터"),
                                fieldWithPath("data.betId").description("베팅 ID"),
                                fieldWithPath("data.refundAmount").description("환불 금액"),
                                fieldWithPath("data.userPointAfter").description("사용자 포인트 잔액"),
                                fieldWithPath("timestamp").description("응답 시간"))
                ));
    }
}
