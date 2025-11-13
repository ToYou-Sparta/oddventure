package org.example.oddventure.domain.match.unit;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oddventure.base.restdocs.RestDocsTestSupport;
import org.example.oddventure.base.restdocs.RestDocsUtils;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.match.controller.MatchController;
import org.example.oddventure.domain.match.dto.request.MatchSearchCondition;
import org.example.oddventure.domain.match.dto.response.MatchResponse;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(MatchController.class)
@Import({SecurityConfig.class, JwtUtil.class})
@WithMockUser(roles = {"USER"})
class MatchControllerTest extends RestDocsTestSupport {

    @MockitoBean
    private MatchService matchService;

    @Autowired
    private ObjectMapper objectMapper;

    private MatchResponse response;

    @BeforeEach
    void setUp() {
        response = new MatchResponse(
                1L,
                "LCK",
                "T1",
                "GEN.G",
                new BigDecimal("10000"),
                new BigDecimal("8000"),
                LocalDateTime.of(2025, 10, 16, 18, 0),
                LocalDateTime.of(2025, 10, 16, 20, 0),
                MatchStatus.SCHEDULED,
                null,
                null,
                153L,
                LocalDateTime.of(2025, 10, 10, 20, 0)
        );
    }

    @Test
    @DisplayName("GET /matches - 매치 목록 조회 성공")
    void getMatches_success() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("startTime").ascending());
        Page<MatchResponse> responsePage = new PageImpl<>(List.of(response), pageable, 1);
        when(matchService.getMatches(pageable)).thenReturn(responsePage);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/matches")
                .param("page", "0")
                .param("size", "10"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].matchId").value(1))
                .andExpect(jsonPath("$.data.content[0].teamB").value("GEN.G"))
                .andExpect(jsonPath("$.data.content[0].status").value("SCHEDULED"))
                .andDo(restDocs.document(
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.content[].matchId").description("매치 ID"),
                                fieldWithPath("data.content[].matchName").description("매치명"),
                                fieldWithPath("data.content[].teamA").description("팀 A"),
                                fieldWithPath("data.content[].teamB").description("팀 B"),
                                fieldWithPath("data.content[].totalAmountA").description("팀 A 베팅 총액"),
                                fieldWithPath("data.content[].totalAmountB").description("팀 B 베팅 총액"),
                                fieldWithPath("data.content[].startTime").description("매치 시작 시간"),
                                fieldWithPath("data.content[].endTime").description("매치 종료 시간"),
                                fieldWithPath("data.content[].status").description("매치 상태"),
                                fieldWithPath("data.content[].winner").type(STRING).description("승리 팀").optional(),
                                fieldWithPath("data.content[].loser").type(STRING).description("패배 팀").optional(),
                                fieldWithPath("data.content[].viewCount").description("조회수"),
                                fieldWithPath("data.content[].createdAt").description("생성일시"),
                                fieldWithPath("data.totalElements").description("전체 데이터 개수"),
                                fieldWithPath("data.totalPages").description("총 페이지 수"),
                                fieldWithPath("data.size").description("페이지당 데이터 개수"),
                                fieldWithPath("data.number").description("현재 페이지 번호")
                        )
                ));
    }

    @Test
    @DisplayName("GET /matches/{matchId} - 매치 상세 조회 성공")
    void getMatchDetail_success() throws Exception {
        // given
        Long matchId = 1L;
        when(matchService.getMatch(matchId)).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/matches/{matchId}", matchId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matchId").value(matchId))
                .andExpect(jsonPath("$.data.teamA").value("T1"))
                .andExpect(jsonPath("$.data.viewCount").value(153L))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("matchId").description("매치 ID")
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.matchId").description("매치 ID"),
                                fieldWithPath("data.matchName").description("매치명"),
                                fieldWithPath("data.teamA").description("팀 A"),
                                fieldWithPath("data.teamB").description("팀 B"),
                                fieldWithPath("data.totalAmountA").description("팀 A 베팅 총액"),
                                fieldWithPath("data.totalAmountB").description("팀 B 베팅 총액"),
                                fieldWithPath("data.startTime").description("매치 시작 시간"),
                                fieldWithPath("data.endTime").description("매치 종료 시간"),
                                fieldWithPath("data.status").description("매치 상태"),
                                fieldWithPath("data.winner").type(STRING).description("승리 팀").optional(),
                                fieldWithPath("data.loser").type(STRING).description("패배 팀").optional(),
                                fieldWithPath("data.viewCount").description("조회수"),
                                fieldWithPath("data.createdAt").description("생성일시")
                        )
                ));
    }

    @Test
    @DisplayName("POST /matches/search - 매치 검색 성공")
    void searchMatches_success() throws Exception {
        // given
        MatchSearchCondition condition = new MatchSearchCondition("", null, null);
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<MatchResponse> responsePage = new PageImpl<>(List.of(response), pageable, 1);
        when(matchService.searchMatches(condition, pageable)).thenReturn(responsePage);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/matches/search")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(condition)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].matchId").value(1))
                .andExpect(jsonPath("$.data.content[0].teamB").value("GEN.G"))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("keyword").description("검색 키워드 (팀명, 매치명 등)").optional(),
                                fieldWithPath("fromDate").type(STRING).description("검색 시작일시 (예: 2025-11-01T00:00:00)")
                                        .optional(),
                                fieldWithPath("toDate").type(STRING).description("검색 종료일시 (예: 2025-11-12T23:59:59)")
                                        .optional()
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.content[].matchId").description("매치 ID"),
                                fieldWithPath("data.content[].matchName").description("매치명"),
                                fieldWithPath("data.content[].teamA").description("팀 A"),
                                fieldWithPath("data.content[].teamB").description("팀 B"),
                                fieldWithPath("data.content[].totalAmountA").description("팀 A 베팅 총액"),
                                fieldWithPath("data.content[].totalAmountB").description("팀 B 베팅 총액"),
                                fieldWithPath("data.content[].startTime").description("매치 시작 시간"),
                                fieldWithPath("data.content[].endTime").description("매치 종료 시간"),
                                fieldWithPath("data.content[].status").description("매치 상태"),
                                fieldWithPath("data.content[].winner").type(STRING).description("승리 팀").optional(),
                                fieldWithPath("data.content[].loser").type(STRING).description("패배 팀").optional(),
                                fieldWithPath("data.content[].viewCount").description("조회수"),
                                fieldWithPath("data.content[].createdAt").description("생성일시"),
                                fieldWithPath("data.totalElements").description("전체 데이터 개수"),
                                fieldWithPath("data.totalPages").description("총 페이지 수"),
                                fieldWithPath("data.size").description("페이지당 데이터 개수"),
                                fieldWithPath("data.number").description("현재 페이지 번호")
                        )
                ));
    }
}