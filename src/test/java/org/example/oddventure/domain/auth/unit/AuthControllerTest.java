package org.example.oddventure.domain.auth.unit;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import org.example.oddventure.base.restdocs.RestDocsTestSupport;
import org.example.oddventure.base.restdocs.RestDocsUtils;
import org.example.oddventure.domain.auth.config.SecurityConfig;
import org.example.oddventure.domain.auth.controller.AuthController;
import org.example.oddventure.domain.auth.dto.AuthUser;
import org.example.oddventure.domain.auth.dto.request.LoginRequest;
import org.example.oddventure.domain.auth.dto.request.SignupRequest;
import org.example.oddventure.domain.auth.dto.request.WithdrawRequest;
import org.example.oddventure.domain.auth.dto.response.TokenResponse;
import org.example.oddventure.domain.auth.dto.response.SignupResponse;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.auth.service.AuthService;
import org.example.oddventure.domain.user.enums.UserRole;
import org.example.oddventure.domain.user.exception.UserErrorCode;
import org.example.oddventure.domain.user.exception.UserException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtUtil.class})
public class AuthControllerTest extends RestDocsTestSupport {

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

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

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.username").value("hello"))
                .andExpect(jsonPath("$.data.point").value("1000"))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("username").description("사용자 이름"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.userId").description("회원 ID"),
                                fieldWithPath("data.username").description("회원 이름"),
                                fieldWithPath("data.email").description("회원 이메일"),
                                fieldWithPath("data.role").description("회원 권한"),
                                fieldWithPath("data.point").description("초기 포인트"),
                                fieldWithPath("data.createdAt").description("회원가입 일시")
                        )
                ));
    }

    @Test
    @DisplayName("POST /signup - 회원가입 실패")
    void signup_fail_exists_email() throws Exception {
        // given
        SignupRequest request = new SignupRequest("hello", "hello@naver.com", "hello123!@#");
        when(authService.signup(request)).thenThrow(new UserException(UserErrorCode.ALREADY_EXIST_EMAIL));

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(UserErrorCode.ALREADY_EXIST_EMAIL.getMessage()))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("username").description("사용자 이름"),
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        RestDocsUtils.errorResponseFields()
                ));
    }

    @Test
    @DisplayName("POST /login - 로그인 성공")
    void login() throws Exception {
        // given
        Long userId = 1L;
        LoginRequest request = new LoginRequest("hello@naver.com", "hello123!@#");
        TokenResponse response = new TokenResponse("accessToken", "refreshToken");
        when(authService.login(request)).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 되었습니다."))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("email").description("사용자 이메일"),
                                fieldWithPath("password").description("비밀번호")
                        ),
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.accessToken").description("액세스 토큰"),
                                fieldWithPath("data.refreshToken").description("리프레시 토큰")
                        )
                ));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /logout - 로그아웃 성공")
    void logout() throws Exception {
        // given
        Long userId = 1L;
        AuthUser authUser = new AuthUser(userId, UserRole.ROLE_USER);
        String testAccessToken = createTestAccessToken(userId);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer " + testAccessToken)
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities()))));
        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 되었습니다."))
                .andExpect(jsonPath("$.success").value(true))
                .andDo(restDocs.document(RestDocsUtils.successWithDataFields()));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /withdraw - 회원 탈퇴 성공")
    void withdraw_success() throws Exception {
        // given
        AuthUser authUser = new AuthUser(1L, UserRole.ROLE_USER);
        WithdrawRequest request = new WithdrawRequest("hello123!@#");

        doNothing().when(authService).withdraw(authUser.id(), request);

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/auth/withdraw")
                .with(authentication(
                        new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities())))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원탈퇴 되었습니다."))
                .andExpect(jsonPath("$.success").value(true))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("password").description("비밀번호 확인")
                        ),
                        RestDocsUtils.successWithDataFields()
                ));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /refresh - 토큰 재발급 성공")
    void refresh_success() throws Exception {
        // given
        String refreshToken = "validRefreshToken";
        TokenResponse response = new TokenResponse("newAccessToken", "newRefreshToken");

        when(authService.refresh(refreshToken)).thenReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)));
        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("토큰이 재발급되었습니다."))
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.success").value(true))
                .andDo(restDocs.document(
                        RestDocsUtils.successWithDataFields(
                                fieldWithPath("data.accessToken").description("새로 발급된 액세스 토큰"),
                                fieldWithPath("data.refreshToken").description("새로 발급된 리프레시 토큰")
                        )
                ));
    }

    private String createTestAccessToken(Long userId) {
        String secretKey = "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLWp3dC10b2tlbi1nZW5lcmF0aW9uLW1pbmltdW0tMzItYnl0ZXM";
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return Jwts.builder()
                .setSubject("1")
                .signWith(Keys.hmacShaKeyFor(decodedKey), SignatureAlgorithm.HS256)
                .compact();
    }
}
