package org.example.oddventure.domain.auth.jwt;

import static org.example.oddventure.domain.auth.jwt.JwtConstants.ACCESS_TOKEN_EXPIRATION;
import static org.example.oddventure.domain.auth.jwt.JwtConstants.BEARER_PREFIX;
import static org.example.oddventure.domain.auth.jwt.JwtConstants.CLAIM_USER_ROLE;
import static org.example.oddventure.domain.auth.jwt.JwtConstants.REFRESH_TOKEN_EXPIRATION;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.auth.exception.AuthErrorCode;
import org.example.oddventure.domain.auth.exception.AuthException;
import org.example.oddventure.domain.user.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * JWT 액세스 토큰 생성 메서드
     *
     * @param userId   로그인한 사용자의 고유 ID
     * @param userRole 사용자 권한
     * @return JWT 토큰 문자열
     */
    public String createAccessToken(Long userId, UserRole userRole) {
        Date date = new Date();

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim(CLAIM_USER_ROLE, userRole)
                .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_EXPIRATION))
                .setIssuedAt(date)
                .setId(UUID.randomUUID().toString())
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    /**
     * JWT 리프레시 토큰 생성 메서드
     *
     * @param userId 로그인한 사용자의 고유 ID
     * @return JWT 토큰 문자열
     */
    public String createRefreshToken(Long userId) {
        Date date = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_EXPIRATION))
                .setIssuedAt(date)
                .setId(UUID.randomUUID().toString())
                .signWith(key, signatureAlgorithm)
                .compact();
    }

    /**
     * Bearer 부분을 제거하는 메서드
     *
     * @param tokenValue 원본 토큰 문자열
     * @return Bearer 접두사가 제거된 순수 JWT 문자열
     * @throws AuthException 토큰이 null 이거나 'Bearer'로 시작 하지 않는 경우
     */
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new AuthException(AuthErrorCode.TOKEN_NOT_FOUND);
    }

    /**
     * payload 부분만 추출하는 메서드
     *
     * @param token JWT 문자열
     * @return Claims 객체
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * JWT 토큰 유효성 검증 메서드
     *
     * @param token 클라이언트가 전달한 JWT 문자열
     * @return 유효한 토큰이면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * JWT 토큰에서 사용자 ID를 추출하는 메서드
     *
     * @param token JWT 문자열
     * @return 토큰의 subject에 저장된 사용자 ID
     */
    public Long extractUserId(String token) {
        return Long.parseLong(extractClaims(token).getSubject());
    }

    /**
     * JWT 토큰에서 사용자 권한을 추출하는 메서드
     *
     * @param token JWT 문자열
     * @return 토큰의 userRole 클레임에 저장된 사용자 권한
     */
    public UserRole extractUserRole(String token) {
        return UserRole.valueOf(extractClaims(token).get(CLAIM_USER_ROLE, String.class));
    }

    /**
     * JWT 토큰에서 JTI(JWT ID)를 추출하는 메서드
     *
     * @param token JWT 문자열
     * @return 토큰의 JTI 값(UUID)
     */
    public String extractJti(String token) {
        return extractClaims(token).getId();
    }
}
