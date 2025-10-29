package org.example.oddventure.domain.auth.unit;

import static org.example.oddventure.domain.auth.jwt.JwtConstants.REFRESH_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.example.oddventure.domain.auth.dto.request.LoginRequest;
import org.example.oddventure.domain.auth.dto.request.SignupRequest;
import org.example.oddventure.domain.auth.dto.request.WithdrawRequest;
import org.example.oddventure.domain.auth.dto.response.AccessTokenResponse;
import org.example.oddventure.domain.auth.dto.response.LoginResponse;
import org.example.oddventure.domain.auth.dto.response.SignupResponse;
import org.example.oddventure.domain.auth.exception.AuthException;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.example.oddventure.domain.auth.service.AuthService;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.enums.UserRole;
import org.example.oddventure.domain.user.exception.UserException;
import org.example.oddventure.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("hello", "hello@naver.com", "hello123!@#");
        String encodedPassword = "$2a$10$eB9vYJzqZK8Zb3Q9gFZJ9uK0xE9gUuZzT1eYwKJvZzFzYxOqL9rP3O";
        User savedUser = createMockUser("hello", "hello@naver.com");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertNotNull(response);
        assertEquals("hello", response.username());
        assertEquals("hello@naver.com", response.email());
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void signup_fail_already_exists() {
        // given
        SignupRequest request = new SignupRequest("hello", "hello@naver.com", "hello123!@#");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when & then
        assertThrows(UserException.class, () -> authService.signup(request));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginRequest request = new LoginRequest("hello@naver.com", "hello123!@#");
        User user = createMockUser("hello", "hello@naver.com");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtUtil.createAccessToken(user.getId(), user.getUserRole())).thenReturn("accessToken");
        when(jwtUtil.createRefreshToken(user.getId())).thenReturn("refreshToken");

        // redis Mocking
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        LoginResponse response = authService.login(request);

        // then
        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void login_fail_unknown_email() {
        // given
        LoginRequest request = new LoginRequest("unknown@naver.com", "unknown123!@#");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // when & then
        assertThrows(AuthException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_invalid_password() {
        // given
        LoginRequest request = new LoginRequest("hello@naver.com", "wrongPassword");
        User user = createMockUser("hello", "hello@naver.com");

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        // when & then
        assertThrows(AuthException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        // given
        Long userId = 1L;

        // when
        authService.logout(userId);

        // then
        verify(redisTemplate).delete(REFRESH_TOKEN_PREFIX + userId);
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdraw_success() {
        // given
        Long userId = 1L;
        WithdrawRequest request = new WithdrawRequest("correctPassword");
        User user = createMockUser("hello", "hello@naver.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);

        // when
        authService.withdraw(userId, request);

        // then
        assertTrue(user.isDeleted());
        verify(redisTemplate).delete(REFRESH_TOKEN_PREFIX + userId);
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 비밀번호 불일치")
    void withdraw_fail_incorrect_password() {
        // given
        Long userId = 1L;
        WithdrawRequest request = new WithdrawRequest("wrongPassword");
        User user = createMockUser("hello", "hello@naver.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        // when & then
        assertThrows(UserException.class, () -> authService.withdraw(userId, request));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 이미 탈퇴된 사용자")
    void withdraw_fail_already_deleted() {
        // given
        Long userId = 1L;
        WithdrawRequest request = new WithdrawRequest("correctPassword");
        User user = createMockUser("hello", "hello@naver.com");
        user.delete();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);

        // when & then
        assertThrows(UserException.class, () -> authService.withdraw(userId, request));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void refresh_success() {
        // given
        String refreshToken = "validRefreshToken";
        Long userId = 1L;
        String newAccessToken = "newAccessToken";

        User user = createMockUser("hello", "hello@naver.com");

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUserId(refreshToken)).thenReturn(userId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_PREFIX + userId)).thenReturn(refreshToken);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.createAccessToken(userId, UserRole.ROLE_USER)).thenReturn(newAccessToken);

        // when
        AccessTokenResponse response = authService.refresh(refreshToken);

        // then
        assertNotNull(response);
        assertEquals(newAccessToken, response.accessToken());
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 유효하지 않은 토큰")
    void refresh_fail_invalid_token() {
        // given
        String refreshToken = "invalidToken";
        when(jwtUtil.validateToken(refreshToken)).thenReturn(false);

        // when & then
        assertThrows(AuthException.class, () -> authService.refresh(refreshToken));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 저장된 토큰과 불일치")
    void refresh_fail_token_mismatch() {
        // given
        String refreshToken = "validRefreshToken";
        Long userId = 1L;

        when(jwtUtil.validateToken(refreshToken)).thenReturn(true);
        when(jwtUtil.extractUserId(refreshToken)).thenReturn(userId);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(REFRESH_TOKEN_PREFIX + userId)).thenReturn("differentToken");

        // when & then
        assertThrows(AuthException.class, () -> authService.refresh(refreshToken));
    }

    private User createMockUser(String username, String email) {
        return User.builder()
                .username(username)
                .email(email)
                .password("$2a$10$eB9vYJzqZK8Zb3Q9gFZJ9uK0xE9gUuZzT1eYwKJvZzFzYxOqL9rP3O")
                .build();
    }
}
