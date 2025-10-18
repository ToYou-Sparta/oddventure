package org.example.oddventure.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("내 프로필 조회 API 성공")
    void getMyProfile_Success() throws Exception {
        // given
        Long userId = 1L;
        UserProfileResponse responseDto = new UserProfileResponse(
                userId,
                "testuser",
                "test@test.com",
                new BigDecimal("1000"),
                UserRole.ROLE_USER,
                LocalDateTime.now()
        );
        given(userService.getUserProfile(userId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/users/me")
                        .with(user(String.valueOf(userId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.email").value("test@test.com"));
    }

    @Test
    @DisplayName("내 프로필 수정 API 성공")
    void updateMyProfile_Success() throws Exception {
        // given
        Long userId = 1L;
        String newUsername = "newName";
        String newEmail = "new@email.com";
        ProfileUpdateRequest requestDto = new ProfileUpdateRequest(newUsername, newEmail);

        UserProfileResponse responseDto = new UserProfileResponse(userId, newUsername, newEmail, new BigDecimal("1000"),
                UserRole.ROLE_USER, LocalDateTime.now());


        given(userService.updateUserProfile(eq(userId), any(ProfileUpdateRequest.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/v1/users/me")
                        .with(user(String.valueOf(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("프로필 수정에 성공했습니다."))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.username").value(newUsername))
                .andExpect(jsonPath("$.data.email").value(newEmail));
    }

    @Test
    @DisplayName("내 프로필 수정 API 실패 - 유효성 검사 실패 (잘못된 이메일 형식)")
    void updateMyProfile_Fail_InvalidEmail() throws Exception {
        // given
        Long userId = 1L;
        ProfileUpdateRequest requestDto = new ProfileUpdateRequest("updatedUser", "invalid-email");

        // when & then
        mockMvc.perform(put("/api/v1/users/me")
                        .with(user(String.valueOf(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 변경 API 성공")
    void updatePassword_Success() throws Exception
    {
        // given
        Long userId = 1L;
        PasswordUpdateRequest requestDto = new PasswordUpdateRequest("currentPassword123!", "newPassword123!");

        // when & then
        mockMvc.perform(put("/api/v1/users/password")
                        .with(user(String.valueOf(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호 변경에 성공했습니다."));
    }

    @Test
    @DisplayName("비밀번호 변경 API 실패 - 유효성 검사 실패 (새 비밀번호 형식 오류)")
    void updatePassword_Fail_InvalidPassword() throws Exception
    {
        // given
        Long userId = 1L;
        PasswordUpdateRequest requestDto = new PasswordUpdateRequest("currentPassword123!", "weak");

        // when & then
        mockMvc.perform(put("/api/v1/users/password")
                        .with(user(String.valueOf(userId)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}