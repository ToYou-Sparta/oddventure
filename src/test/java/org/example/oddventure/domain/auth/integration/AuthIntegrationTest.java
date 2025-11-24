package org.example.oddventure.domain.auth.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.example.oddventure.domain.auth.dto.request.LoginRequest;
import org.example.oddventure.domain.auth.dto.request.SignupRequest;
import org.example.oddventure.domain.auth.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("인증/인가 전체 흐름 통합 테스트")
public class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    private static String accessToken;
    private static String refreshToken;

    private final String signupUrl = "/api/v1/auth/signup";
    private final String loginUrl = "/api/v1/auth/login";
    private final String refreshUrl = "/api/v1/auth/refresh";
    private final String logoutUrl = "/api/v1/auth/logout";
    private final String protectedUrl = "/api/v1/users/me";

    @Test
    @Order(1)
    @DisplayName("회원가입 성공")
    void signup_success() {
        SignupRequest request = new SignupRequest("tester", "test@naver.com", "hello123!@#");
        ResponseEntity<String> response = restTemplate.postForEntity(signupUrl, request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @Order(2)
    @DisplayName("로그인 성공 → AccessToken과 RefreshToken 발급")
    void login_success() {
        LoginRequest request = new LoginRequest("test@naver.com", "hello123!@#");

        ResponseEntity<Map> response = restTemplate.exchange(
                loginUrl,
                HttpMethod.POST,
                new HttpEntity<>(request),
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map data = (Map) response.getBody().get("data");
        accessToken = (String) data.get("accessToken");
        refreshToken = (String) data.get("refreshToken");

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
    }

    @Test
    @Order(3)
    @DisplayName("AccessToken으로 보호된 API 접근 성공")
    void access_protected_api_success() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> http = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                protectedUrl,
                HttpMethod.GET,
                http,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(4)
    @DisplayName("RefreshToken으로 새 토큰 발급 → 토큰 로테이션 성공")
    void refresh_token_rotation_success() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "refreshToken=" + refreshToken);

        HttpEntity<Void> http = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                refreshUrl,
                HttpMethod.POST,
                http,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map data = (Map) response.getBody().get("data");
        String newAccessToken = (String) data.get("accessToken");
        String newRefreshToken = (String) data.get("refreshToken");

        assertNotEquals(refreshToken, newRefreshToken);
        assertNotEquals(accessToken, newAccessToken);

        // 새로운 토큰으로 업데이트
        accessToken = newAccessToken;
        refreshToken = newRefreshToken;
    }

    @Test
    @Order(5)
    @DisplayName("로테이션 후 이전 RefreshToken 사용 시 403 반환")
    void use_old_refresh_token_fail() {
        String oldRtButValidJwt = jwtUtil.createRefreshToken(1L);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "refreshToken=" + oldRtButValidJwt);

        HttpEntity<Void> http = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                refreshUrl,
                HttpMethod.POST,
                http,
                String.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode()); // mismatch
    }

    @Test
    @Order(6)
    @DisplayName("로그아웃 시 AccessToken 블랙리스트 등록")
    void accessToken_blacklisted() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> http = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                logoutUrl,
                HttpMethod.POST,
                http,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        String jti = jwtUtil.extractJti(accessToken);
        Boolean exists = redisTemplate.hasKey("blacklist:access_token:" + jti);

        assertTrue(exists);
    }

    @Test
    @Order(7)
    @DisplayName("블랙리스트 AccessToken으로 보호된 API 접근 시 401 반환")
    void access_with_blacklisted_token_fail() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> http = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                protectedUrl,
                HttpMethod.GET,
                http,
                String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
