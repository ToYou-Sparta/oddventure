package org.example.oddventure.domain.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.controller.AdminController;
import org.example.oddventure.domain.admin.dto.request.MatchCreateRequest;
import org.example.oddventure.domain.admin.dto.request.MatchUpdateRequest;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.MatchAdminResponse;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.admin.exception.AdminErrorCode;
import org.example.oddventure.domain.admin.service.AdminService;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.match.enums.MatchStatus;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtUtil.class})
@WithMockUser(roles = {"ADMIN"})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @Test
    @DisplayName("매치 생성 성공")
    void createMatch_Success() throws Exception {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        MatchCreateRequest request = new MatchCreateRequest("LCK", "T1", "Gen.G", startTime);
        MatchAdminResponse response = new MatchAdminResponse(1L, "LCK", "T1", "Gen.G", startTime,
                MatchStatus.SCHEDULED);
        given(adminService.createMatch(any(MatchCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/admin/matches")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.teamA").value("T1"))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("매치 생성 실패 - 유효성 검사 실패")
    void createMatch_Fail_InvalidInput() throws Exception {
        // given
        MatchCreateRequest request = new MatchCreateRequest("LCK", "", "Gen.G", LocalDateTime.now().plusDays(1));

        // when & then
        mockMvc.perform(post("/api/v1/admin/matches")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("매치 정보 수정 성공")
    void updateMatch_Success() throws Exception {
        // given
        Long matchId = 1L;
        LocalDateTime newStartTime = LocalDateTime.now().plusDays(2).withNano(0);

        MatchUpdateRequest request = new MatchUpdateRequest(
                "LCK", "New Team A", "New Team B", newStartTime, MatchStatus.ONGOING
        );
        MatchAdminResponse response = new MatchAdminResponse(
                matchId, "LCK", "New Team A", "New Team B", newStartTime, MatchStatus.ONGOING
        );

        given(adminService.updateMatch(any(Long.class), any(MatchUpdateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/v1/admin/matches/{matchId}", matchId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.matchId").value(matchId))
                .andExpect(jsonPath("$.data.teamA").value("New Team A"))
                .andExpect(jsonPath("$.data.status").value("ONGOING"));
    }

    @Test
    @DisplayName("사용자 목록 조회 API 성공")
    void getAllUsers_Success() throws Exception {
        // given
        Pageable pageable = PageRequest.of(0, 5);
        List<UserAdminResponse> userList = List.of(
                new UserAdminResponse(1L, "testuser1", "test1@email.com", new BigDecimal("1000"), UserRole.ROLE_USER,
                        LocalDateTime.now()));
        Page<UserAdminResponse> mockResponsePage = new PageImpl<>(userList, pageable, 1);
        given(adminService.getAllUsers(any(), any(), any(Pageable.class))).willReturn(mockResponsePage);

        // when & then
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(user("admin").roles("ADMIN"))
                        .param("page", "0").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.content[0].userId").value(1L));
    }

    @Test
    @DisplayName("사용자 상세 조회 API 성공")
    void getUserDetails_Success() throws Exception {
        // given
        Long userId = 1L;
        UserAdminResponse responseDto = new UserAdminResponse(userId, "testuser", "test@test.com",
                new BigDecimal("1000"), UserRole.ROLE_USER, LocalDateTime.now());
        given(adminService.getUserDetails(userId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/admin/users/{userId}", userId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 상세 정보 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.email").value("test@test.com"));
    }

    @Test
    @DisplayName("사용자 상세 조회 API 실패 - 사용자를 찾을 수 없음")
    void getUserDetails_Fail_UserNotFound() throws Exception {
        // given
        Long userId = 999L;
        given(adminService.getUserDetails(userId)).willThrow(new GlobalException(AdminErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/admin/users/{userId}", userId)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("해당 사용자를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("사용자 포인트 지급 API 성공")
    void adjustUserPoints_Success() throws Exception {
        // given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("5000");
        PointAdjustRequest request = new PointAdjustRequest(amount, "베팅 승리 보상");
        PointAdjustResponse response = new PointAdjustResponse(userId, "testuser", amount, new BigDecimal("6000"));

        given(adminService.adjustUserPoints(eq(userId), any(PointAdjustRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/admin/users/{userId}/points", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("포인트 지급에 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.finalBalance").value(6000));
    }

    @Test
    @DisplayName("사용자 포인트 지급 API 실패")
    void adjustUserPoints_Fail_NoReason() throws Exception {
        // given
        Long userId = 1L;
        // reason이 비어있는 잘못된 요청
        PointAdjustRequest request = new PointAdjustRequest(new BigDecimal("5000"), "");

        // when & then
        mockMvc.perform(post("/api/v1/admin/users/{userId}/points", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}