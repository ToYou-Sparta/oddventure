package org.example.oddventure.domain.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.domain.auth.dto.AuthUser;
import org.example.oddventure.domain.auth.exception.AuthErrorCode;
import org.example.oddventure.domain.user.enums.UserRole;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// JWT 토큰을 검증하고 사용자 정보 추출
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpRequest,
            @NonNull HttpServletResponse httpResponse,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        // Authorization 헤더에서 토큰 추출
        String authorizationHeader = httpRequest.getHeader("Authorization");

        // Authorization 헤더가 있고 "Bearer "로 시작하는지 확인
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            // Bearer 접두사 제거
            String jwt = jwtUtil.substringToken(authorizationHeader);
            try {

                // JWT 토큰 파싱 및 검증
                Claims claims = jwtUtil.extractClaims(jwt);

                // SecurityContext에 이미 인증 정보가 있는지 확인
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    setAuthentication(claims);
                }
            } catch (SecurityException | MalformedJwtException e) {
                log.error("Invalid JWT signature", e);
                sendErrorResponse(httpResponse, AuthErrorCode.JWT_INVALID_SIGNATURE);
                return;
            } catch (ExpiredJwtException e) {
                log.error("Expired JWT token", e);
                sendErrorResponse(httpResponse, AuthErrorCode.JWT_EXPIRED);
                return;
            } catch (UnsupportedJwtException e) {
                log.error("Unsupported JWT token", e);
                sendErrorResponse(httpResponse, AuthErrorCode.JWT_UNSUPPORTED);
                return;
            } catch (Exception e) {
                log.error("Internal server error", e);
                sendErrorResponse(httpResponse, AuthErrorCode.JWT_INTERNAL_ERROR);
                return;
            }
        }
        chain.doFilter(httpRequest, httpResponse);
    }

    /**
     * JWT Claims에서 사용자 정보를 추출하여 SecurityContext에 저장하는 메서드
     *
     * @param claims JWT 토큰에서 추출한 정보
     */
    private void setAuthentication(Claims claims) {
        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        String username = claims.get("username", String.class);
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));

        AuthUser authUser = new AuthUser(userId, username, email, userRole);
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(authUser);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * HTTP 에러 응답을 JSON 형태로 클라이언트에게 전송하는 메서드
     *
     * @param httpResponse 클라이언트에게 보낼 응답
     * @param errorCode    AuthErrorCode(상태 코드 및 에러 메시지 포함)
     * @throws IOException 응답 작성 중 IO 오류 발생 시
     */
    private void sendErrorResponse(HttpServletResponse httpResponse, AuthErrorCode errorCode)
            throws IOException {
        httpResponse.setStatus(errorCode.getHttpStatus().value());
        httpResponse.setContentType("application/json;charset=UTF-8");
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", errorCode.getHttpStatus().name());
        errorResponse.put("code", errorCode.getHttpStatus().value());
        errorResponse.put("message", errorCode.getMessage());
        httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
