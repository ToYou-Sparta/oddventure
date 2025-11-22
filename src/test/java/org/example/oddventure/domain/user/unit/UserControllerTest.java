package org.example.oddventure.domain.user.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.oddventure.base.WithMockAuthUser;
import org.example.oddventure.base.restdocs.RestDocsTestSupport;
import org.example.oddventure.base.restdocs.RestDocsUtils;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.user.controller.UserController;
import org.example.oddventure.domain.user.dto.request.PasswordUpdateRequest;
import org.example.oddventure.domain.user.dto.request.ProfileUpdateRequest;
import org.example.oddventure.domain.user.dto.response.UserProfileResponse;
import org.example.oddventure.domain.user.enums.UserRole;
import org.example.oddventure.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtUtil.class})
@WithMockAuthUser(userId = 1L, role = UserRole.ROLE_USER)
public class UserControllerTest extends RestDocsTestSupport {

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("GET /me - 내 프로필 조회 API")
    void getMyProfile_Success() throws Exception {
        // given
        Long userId = 1L;
        UserProfileResponse responseDto = new UserProfileResponse(
                userId, "testuser", "test@test.com", new BigDecimal("1000"), UserRole.ROLE_USER, LocalDateTime.now()
        );
        given(userService.getUserProfile(userId)).willReturn(responseDto);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andDo(restDocs.document(
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
    @DisplayName("PATCH /me - 내 프로필 수정 API")
    void updateMyProfile_Success() throws Exception {
        // given
        Long userId = 1L;
        String newUsername = "newName";
        String newEmail = "new@email.com";
        ProfileUpdateRequest requestDto = new ProfileUpdateRequest(newUsername, newEmail);

        UserProfileResponse responseDto = new UserProfileResponse(userId, newUsername, newEmail, new BigDecimal("1000"),
                UserRole.ROLE_USER, LocalDateTime.now());
        given(userService.updateUserProfile(eq(userId), any(ProfileUpdateRequest.class))).willReturn(responseDto);

        // when
        ResultActions result = mockMvc.perform(patch("/api/v1/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value(newUsername))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("username").description("변경할 사용자 이름 (2~10자, 선택적)").optional(),
                                fieldWithPath("email").description("변경할 이메일 (형식 준수, 선택적)").optional()
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.userId").description("사용자 ID"),
                                fieldWithPath("data.username").description("수정된 사용자 이름"),
                                fieldWithPath("data.email").description("수정된 이메일"),
                                fieldWithPath("data.point").description("보유 포인트"),
                                fieldWithPath("data.role").description("사용자 역할"),
                                fieldWithPath("data.createdAt").description("가입 일시")
                        )
                ));
    }

    @Test
    @DisplayName("PATCH /password - 비밀번호 변경 API")
    void updatePassword_Success() throws Exception {
        // given
        PasswordUpdateRequest requestDto = new PasswordUpdateRequest("currentPassword123!", "newPassword123!");

        // when
        ResultActions result = mockMvc.perform(patch("/api/v1/users/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호 변경에 성공했습니다."))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("currentPassword").description("현재 비밀번호"),
                                fieldWithPath("newPassword").description("새 비밀번호 (영문, 숫자, 특수문자 포함 8~20자)")
                        ),
                        RestDocsUtils.successWithDataFields()
                ));
    }
}