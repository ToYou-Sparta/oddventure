package org.example.oddventure.domain.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.example.oddventure.base.WithMockAuthUser;
import org.example.oddventure.base.restdocs.RestDocsTestSupport;
import org.example.oddventure.base.restdocs.RestDocsUtils;
import org.example.oddventure.common.exception.GlobalException;
import org.example.oddventure.domain.admin.controller.AdminUserController;
import org.example.oddventure.domain.admin.dto.request.PointAdjustRequest;
import org.example.oddventure.domain.admin.dto.response.PointAdjustResponse;
import org.example.oddventure.domain.admin.dto.response.UserAdminResponse;
import org.example.oddventure.domain.admin.exception.AdminErrorCode;
import org.example.oddventure.domain.admin.service.AdminUserService;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
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
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, JwtUtil.class, RestDocsUtils.class})
@WithMockAuthUser(userId = 1L, role = UserRole.ROLE_ADMIN)
public class AdminUserControllerTest extends RestDocsTestSupport {

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("사용자 목록 조회 API 성공")
    void getAllUsers_Success() throws Exception {
        // given
        String email = "test";
        String username = "user";
        Pageable pageable = PageRequest.of(0, 5);

        List<UserAdminResponse> userList = List.of(
                new UserAdminResponse(1L, "testuser1", "test1@email.com", new BigDecimal("1000"), UserRole.ROLE_USER,
                        LocalDateTime.now())
        );
        Page<UserAdminResponse> mockResponsePage = new PageImpl<>(userList, pageable, 1);

        given(adminUserService.getAllUsers(eq(email), eq(username), any(Pageable.class))).willReturn(mockResponsePage);

        // when & then
        mockMvc.perform(get("/api/v1/admin/users")
                        .param("email", email)
                        .param("username", username)
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.number").value(0))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("email").description("검색할 이메일 (선택적)").optional(),
                                parameterWithName("username").description("검색할 사용자 이름 (선택적)").optional(),
                                parameterWithName("page").description("페이지 번호 (0부터 시작)"),
                                parameterWithName("size").description("페이지 당 항목 수")
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.content[]").description("사용자 정보 목록"),
                                fieldWithPath("data.content[].userId").description("사용자 ID"),
                                fieldWithPath("data.content[].username").description("사용자 이름"),
                                fieldWithPath("data.content[].email").description("사용자 이메일"),
                                fieldWithPath("data.content[].point").description("보유 포인트"),
                                fieldWithPath("data.content[].role").description("사용자 역할"),
                                fieldWithPath("data.content[].createdAt").description("가입 일시"),
                                fieldWithPath("data.totalElements").description("전체 항목 수"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.size").description("페이지 크기"),
                                fieldWithPath("data.number").description("현재 페이지 번호 (0부터 시작)")
                        )
                ));
    }

    @Test
    @DisplayName("사용자 상세 조회 API 성공")
    void getUserDetails_Success() throws Exception {
        // given
        Long userId = 1L;
        UserAdminResponse responseDto = new UserAdminResponse(userId, "testuser", "test@test.com",
                new BigDecimal("1000"), UserRole.ROLE_USER, LocalDateTime.now());
        given(adminUserService.getUserDetails(userId)).willReturn(responseDto);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/admin/users/{userId}", userId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("userId").description("조회할 사용자 ID")
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.userId").description("사용자 ID"),
                                fieldWithPath("data.username").description("사용자 이름"),
                                fieldWithPath("data.email").description("사용자 이메일"),
                                fieldWithPath("data.point").description("보유 포인트"),
                                fieldWithPath("data.role").description("사용자 역할"),
                                fieldWithPath("data.createdAt").description("가입 일시")
                        )
                ));
    }

    @Test
    @DisplayName("사용자 상세 조회 API 실패 - 사용자를 찾을 수 없음")
    void getUserDetails_Fail_UserNotFound() throws Exception {
        // given
        Long userId = 999L;
        given(adminUserService.getUserDetails(userId)).willThrow(new GlobalException(AdminErrorCode.USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/admin/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("userId").description("조회할 사용자 ID")
                        ),
                        RestDocsUtils.errorResponseFields()
                ));
    }

    @Test
    @DisplayName("사용자 포인트 지급 API 성공")
    void adjustUserPoints_Success() throws Exception {
        // given
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("5000");
        PointAdjustRequest request = new PointAdjustRequest(amount, "베팅 승리 보상");
        PointAdjustResponse response = new PointAdjustResponse(userId, "testuser", amount, new BigDecimal("6000"));
        given(adminUserService.adjustUserPoints(eq(userId), any(PointAdjustRequest.class))).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/admin/users/{userId}/points", userId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("userId").description("포인트를 조정할 사용자 ID")
                        ),
                        requestFields(
                                fieldWithPath("amount").description("조정할 포인트 (양수: 지급)"),
                                fieldWithPath("reason").description("조정 사유 (로그 기록용)")
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.userId").description("사용자 ID"),
                                fieldWithPath("data.username").description("사용자 이름"),
                                fieldWithPath("data.adjustedAmount").description("조정된 포인트"),
                                fieldWithPath("data.finalBalance").description("조정 후 최종 포인트")
                        )
                ));
    }

    @Test
    @DisplayName("사용자 포인트 지급 API 실패 - 유효성 검사 실패")
    void adjustUserPoints_Fail_InvalidInput() throws Exception {
        // given
        Long userId = 1L;
        PointAdjustRequest request = new PointAdjustRequest(new BigDecimal("5000"), "");

        // when & then
        mockMvc.perform(post("/api/v1/admin/users/{userId}/points", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("userId").description("포인트를 조정할 사용자 ID")
                        ),
                        RestDocsUtils.errorResponseFields()
                ));
    }
}
