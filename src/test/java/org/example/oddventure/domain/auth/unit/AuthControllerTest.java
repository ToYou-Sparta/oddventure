package org.example.oddventure.domain.auth.unit;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.controller.AuthController;
import org.example.oddventure.domain.auth.dto.AuthUser;
import org.example.oddventure.domain.auth.dto.request.LoginRequest;
import org.example.oddventure.domain.auth.dto.request.SignupRequest;
import org.example.oddventure.domain.auth.dto.request.WithdrawRequest;
import org.example.oddventure.domain.auth.dto.response.AccessTokenResponse;
import org.example.oddventure.domain.auth.dto.response.LoginResponse;
import org.example.oddventure.domain.auth.dto.response.SignupResponse;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.auth.service.AuthService;
import org.example.oddventure.domain.user.enums.UserRole;
import org.example.oddventure.domain.user.exception.InvalidUserException;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtUtil.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /signup - 회원가입 성공")
    void signup_success() throws Exception {

        // given
        Long userId = 1L;
        SignupRequest request = new SignupRequest("hello", "hello@naver.com", "hello123!@#");
        SignupResponse response = new SignupResponse(
                userId,
                "hello",
                "hello@naver.com",
                UserRole.ROLE_USER,
                new BigDecimal("1000"),
                LocalDateTime.now()
        );
        when(authService.signup(request)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.username").value("hello"))
                .andExpect(jsonPath("$.data.point").value("1000"));
    }

    @Test
    @DisplayName("POST /signup - 회원가입 실패")
    void signup_fail_exists_email() throws Exception {

        // given
        SignupRequest request = new SignupRequest("hello", "hello@naver.com", "hello123!@#");

        when(authService.signup(request))
                .thenThrow(new InvalidUserException(UserErrorCode.ALREADY_EXIST_EMAIL));

        // when & then
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 존재하는 이메일 입니다."));
    }

    @Test
    @DisplayName("POST /login - 로그인 성공")
    void login() throws Exception {

        // given
        Long userId = 1L;
        LoginRequest request = new LoginRequest("hello@naver.com", "hello123!@#");
        LoginResponse response = new LoginResponse("accessToken", "refreshToken");
        when(authService.login(request)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 되었습니다."))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /logout - 로그아웃 성공")
    void logout() throws Exception {

        // given
        AuthUser authUser = new AuthUser(1L, UserRole.ROLE_USER);
        doNothing().when(authService).logout(authUser.id());

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /withdraw - 회원 탈퇴 성공")
    void withdraw_success() throws Exception {

        // given
        AuthUser authUser = new AuthUser(1L, UserRole.ROLE_USER);
        WithdrawRequest request = new WithdrawRequest("hello123!@#");

        doNothing().when(authService).withdraw(authUser.id(), request);

        // when & then
        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원탈퇴 되었습니다."))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /refresh - 토큰 재발급 성공")
    void refresh_success() throws Exception {

        // given
        String refreshToken = "validRefreshToken";
        AccessTokenResponse response = new AccessTokenResponse("newAccessToken");

        when(authService.refresh(refreshToken)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("토큰이 재발급되었습니다."))
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.success").value(true));
    }
}
