package org.example.oddventure.domain.auth.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import org.example.oddventure.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        String secretKey = Base64.getEncoder().encodeToString("secretkeyabcdefghijklmnopqrstuvwxyz".getBytes());
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secretKey);
        jwtUtil.init();
    }

    @Test
    @DisplayName("액세스 토큰 생성 성공")
    void createAccessToken_success() {

        // given
        Long userId = 1L;
        UserRole role = UserRole.ROLE_USER;

        // when
        String token = jwtUtil.createAccessToken(userId, role);

        // then
        assertEquals(role, jwtUtil.extractUserRole(token));
    }

    @Test
    @DisplayName("리프레시 토큰 생성 및 검증 성공")
    void createRefreshToken_success() {

        // given
        Long userId = 2L;

        // when
        String token = jwtUtil.createRefreshToken(userId);
        boolean isValid = jwtUtil.validateToken(token);

        // then
        assertTrue(isValid);
        assertEquals(userId, jwtUtil.extractUserId(token));
    }

    @Test
    @DisplayName("Bearer 접두사 제거 성공")
    void substringToken_success() {

        // given
        String bearerToken = "Bearer abc.def.ghi";

        // when
        String result = jwtUtil.substringToken(bearerToken);

        // then
        assertEquals("abc.def.ghi", result);
    }

    @Test
    @DisplayName("토큰 유효성 검증 실패")
    void validateToken_fail() {

        // given
        String invalidToken = "invalid.token.value";

        // when & then
        assertFalse(jwtUtil.validateToken(invalidToken));
    }
}
