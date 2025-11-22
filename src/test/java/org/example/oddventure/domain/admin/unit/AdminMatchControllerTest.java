package org.example.oddventure.domain.admin.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oddventure.base.WithMockAuthUser;
import org.example.oddventure.base.restdocs.RestDocsTestSupport;
import org.example.oddventure.base.restdocs.RestDocsUtils;
import org.example.oddventure.domain.admin.controller.AdminMatchController;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.response.MatchCreateAdminResponse;
import org.example.oddventure.domain.admin.dto.response.MatchUpdateAdminResponse;
import org.example.oddventure.domain.admin.service.AdminMatchService;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.match.enums.MatchStatus;
import org.example.oddventure.domain.match.service.MatchService;
import org.example.oddventure.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.example.oddventure.domain.match.scheduler.MatchEsSyncScheduler;

@WebMvcTest(AdminMatchController.class)
@Import({SecurityConfig.class, JwtUtil.class, RestDocsUtils.class})
@WithMockAuthUser(userId = 1L, role = UserRole.ROLE_ADMIN)
public class AdminMatchControllerTest extends RestDocsTestSupport {

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    private AdminMatchService adminMatchService;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private MatchEsSyncScheduler matchEsSyncScheduler;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("매치 생성 성공")
    void createMatch_Success() throws Exception {
        // given
//        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
//        MatchCreateRequest request = new MatchCreateRequest("LCK", "T1", "Gen.G", startTime);
//        MatchCreateAdminResponse response = new MatchCreateAdminResponse(1L, "LCK", "T1", "Gen.G", startTime, MatchStatus.SCHEDULED);
        MatchCreateAdminResponse response = MatchCreateAdminResponse.builder()
                .totalMatches(1)
                .fetchIds(List.of(1L))
                .build();
        given(adminMatchService.createMatch()).willReturn(response);

        // when&then
        mockMvc.perform(post("/api/v1/admin/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(restDocs.document(
//                        requestFields(
//                                fieldWithPath("matchName").description("경기 이름 (예: LCK)"),
//                                fieldWithPath("teamA").description("A팀 이름"),
//                                fieldWithPath("teamB").description("B팀 이름"),
//                                fieldWithPath("startTime").description("경기 시작 시간")
//                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.totalMatches").description("생성된 매치 개수"),
                                fieldWithPath("data.fetchIds").description("생성된 패치 ID 목록")
                        )
                ));
    }

//    @Test
//    @DisplayName("매치 생성 실패 - 유효성 검사 실패")
//    void createMatch_Fail_InvalidInput() throws Exception {
//        // given
//        MatchCreateRequest request = new MatchCreateRequest("LCK", "", "Gen.G", LocalDateTime.now().plusDays(1));
//
//        // when & then
//        mockMvc.perform(post("/api/v1/admin/matches")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andDo(restDocs.document(
//                        RestDocsUtils.errorResponseFields()
//                ));
//    }

    @Test
    @DisplayName("매치 정보 수정 성공")
    void updateMatch_Success() throws Exception {
        // given
        Long matchId = 1L;
        LocalDateTime newStartTime = LocalDateTime.now().plusDays(2).withNano(0);
        MatchUpdateRequest request = new MatchUpdateRequest("LCK", "New Team A", "New Team B", newStartTime,
                MatchStatus.ONGOING);
        MatchUpdateAdminResponse response = new MatchUpdateAdminResponse(matchId, "LCK", "New Team A", "New Team B",
                newStartTime,
                MatchStatus.ONGOING);
        given(matchService.updateMatch(any(Long.class), any(MatchUpdateRequest.class))).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(patch("/api/v1/admin/matches/{matchId}", matchId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("matchId").description("수정할 경기 ID")
                        ),
                        requestFields(
                                fieldWithPath("matchName").description("변경할 경기 이름 (선택적)").optional(),
                                fieldWithPath("teamA").description("변경할 A팀 이름 (선택적)").optional(),
                                fieldWithPath("teamB").description("변경할 B팀 이름 (선택적)").optional(),
                                fieldWithPath("startTime").description("변경할 경기 시작 시간 (선택적)").optional(),
                                fieldWithPath("status").description("변경할 경기 상태 (선택적)").optional()
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.matchId").description("수정된 경기 ID"),
                                fieldWithPath("data.matchName").description("수정된 경기 이름"),
                                fieldWithPath("data.teamA").description("수정된 A팀 이름"),
                                fieldWithPath("data.teamB").description("수정된 B팀 이름"),
                                fieldWithPath("data.startTime").description("수정된 경기 시작 시간"),
                                fieldWithPath("data.status").description("수정된 경기 상태 (SCHEDULED, ONGOING, FINISHED)")
                        )
                ));
    }
}
