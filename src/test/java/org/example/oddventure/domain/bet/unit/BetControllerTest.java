package org.example.oddventure.domain.bet.unit;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oddventure.base.WithMockAuthUser;
import org.example.oddventure.base.restdocs.RestDocsTestSupport;
import org.example.oddventure.base.restdocs.RestDocsUtils;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.bet.controller.BetController;
import org.example.oddventure.domain.bet.dto.request.BetCreateRequest;
import org.example.oddventure.domain.bet.dto.response.BetCreateResponse;
import org.example.oddventure.domain.bet.dto.response.BetDeleteResponse;
import org.example.oddventure.domain.bet.dto.response.BetResponse;
import org.example.oddventure.domain.bet.enums.SelectedTeam;
import org.example.oddventure.domain.bet.service.BetService;
import org.example.oddventure.domain.match.dto.response.MatchBetResponse;
import org.example.oddventure.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(BetController.class)
@Import({SecurityConfig.class, JwtUtil.class})
@WithMockAuthUser(userId = 1, role = UserRole.ROLE_USER)
public class BetControllerTest extends RestDocsTestSupport {

    @MockitoBean
    private BetService betService;

    @Autowired
    private ObjectMapper objectMapper;

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
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.betId").description("베팅 ID"),
                                fieldWithPath("data.userId").description("사용자 ID"),
                                fieldWithPath("data.selectedTeam").description("선택한 팀"),
                                fieldWithPath("data.selectedTeamName").description("선택한 팀 이름"),
                                fieldWithPath("data.betAmount").description("베팅 금액"),
                                fieldWithPath("data.oddsAtBetting").description("베팅 시점 배당률"),
                                fieldWithPath("data.userPointAfter").description("사용자 포인트 잔액")
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
        mockMvc.perform(delete("/api/v1/bets/{betId}", betId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("betId").description("베팅 ID")
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.betId").description("베팅 ID"),
                                fieldWithPath("data.refundAmount").description("환불 금액"),
                                fieldWithPath("data.userPointAfter").description("사용자 포인트 잔액")
                        )
                ));
    }

    @Test
    @DisplayName("GET /bets/me 베팅 내역 조회 성공")
    public void getBet_success() throws Exception {
        //given
        Long userId = 1L;
        Long betId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Long matchId = 1L;
        MatchBetResponse matchBetResponse = MatchBetResponse.builder()
                .matchId(matchId)
                .teamA("T1")
                .teamB("GEN.G")
                .startTime(LocalDateTime.now().plusDays(1))
                .build();

        BetResponse betResponse = BetResponse.builder()
                .betId(betId)
                .matchBetResponse(matchBetResponse)
                .selectedTeam(SelectedTeam.Team_A)
                .betAmount(BigDecimal.valueOf(1000))
                .oddsAtBetting(BigDecimal.valueOf(1.50))
                .isWin(false)
                .createdAt(LocalDateTime.now())
                .build();

        Page<BetResponse> responses = new PageImpl<>(List.of(betResponse), pageable, 1);

        when(betService.getBets(userId, pageable)).thenReturn(responses);

        //when & then
        mockMvc.perform(get("/api/v1/bets/me")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.content[].betId").description("베팅 ID"),
                                fieldWithPath("data.content[].matchBetResponse.matchId").description("경기 ID"),
                                fieldWithPath("data.content[].matchBetResponse.teamA").description("팀 A"),
                                fieldWithPath("data.content[].matchBetResponse.teamB").description("팀 B"),
                                fieldWithPath("data.content[].matchBetResponse.startTime").description("경기 시작 시간"),
                                fieldWithPath("data.content[].selectedTeam").description("선택한 팀"),
                                fieldWithPath("data.content[].betAmount").description("베팅 금액"),
                                fieldWithPath("data.content[].oddsAtBetting").description("베팅 시점 배당률"),
                                fieldWithPath("data.content[].isWin").description("베팅 성공 여부"),
                                fieldWithPath("data.content[].createdAt").description("베팅 생성 시간"),
                                fieldWithPath("data.totalElements").description("전체 데이터 개수"),
                                fieldWithPath("data.totalPages").description("총 페이지 수"),
                                fieldWithPath("data.size").description("페이지 당 데이터 개수"),
                                fieldWithPath("data.number").description("현재 페이지 번호")
                        )
                ));
    }
}