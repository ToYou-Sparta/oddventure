package org.example.oddventure.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.common.exception.InvalidAuthException;
import org.example.oddventure.domain.common.exception.AuthErrorCode;
import org.example.oddventure.domain.user.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    //JWT 앞에 붙는 접두사
    private static final String BEARER_PREFIX = "Bearer ";

    //토큰 유효 시간 60분
    private static final long TOKEN_TIME = 60 * 60 * 1000L; // 60분

    /**
     * applictation.yml 에서 jwt.seceret.key 값을 가져 온다.
     *
     * 환경변수로 관리
     */
    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;

    //암호화 알고림즘 HS256
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    /**
     * JWT 토큰 생성 메서드
     *
     * @param userId
     * @param username
     * @param email
     * @param userRole
     * @return Bearer 접두사가 붙은 JWT 토큰 문자열
     */
    public String createToken(Long userId, String username, String email, UserRole userRole) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .claim("email", email)
                        .claim("username", username)
                        .claim("userRole", userRole)
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간: 현재 시간 + 60분
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    /**
     * Bearer 부분을 제거하는 메서드
     *
     * @param tokenValue 원본 토큰 문자열
     * @return Bearer 접두사가 제거된 순수 JWT 문자열
     * @throws InvalidAuthException 토큰이 null 이거나 'Bearer'로 시작 하지 않는 경우
     */
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new InvalidAuthException(AuthErrorCode.JWT_CANNOT_FIND_TOKEN);
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
}
