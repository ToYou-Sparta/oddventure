package org.example.oddventure.domain.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
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

    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN = 60 * 60 * 1000L; // 60분
    private static final long REFRESH_TOKEN = 7 * 24 * 60 * 60 * 1000L; // 7일

    //암호화 알고림즘 HS256
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    /**
     * applictation.yml 에서 jwt.seceret.key 값을 가져옴
     */
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
     * @return Bearer 접두사가 붙은 JWT 토큰 문자열
     */
    public String createAccessToken(Long userId, UserRole userRole) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .claim("userRole", userRole)
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN))
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    /**
     * JWT 리프레시 토큰 생성 메서드
     *
     * @param userId 로그인한 사용자의 고유 ID
     * @return 순수 JWT 토큰 문자열
     */
    public String createRefreshToken(Long userId) {
        Date date = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setExpiration(new Date(date.getTime() + REFRESH_TOKEN))
                .setIssuedAt(date)
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
        throw new AuthException(AuthErrorCode.JWT_CANNOT_FIND_TOKEN);
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
