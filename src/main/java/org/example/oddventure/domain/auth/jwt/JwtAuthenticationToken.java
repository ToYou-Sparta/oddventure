package org.example.oddventure.domain.auth.jwt;

import org.example.oddventure.domain.auth.dto.AuthUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;

// AuthUser를 Spring Security에서 사용하기 위한 작업을 하는 클래스
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthUser authUser;

    /**
     * AuthUser를 받아 인증 토큰을 만드는 메서드
     *
     * @param authUser 인증된 사용자 정보
     */
    public JwtAuthenticationToken(AuthUser authUser) {
        super(authUser.getAuthorities());
        this.authUser = authUser;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        // JWT 기반 인증은 별도의 자격 증명(credentials)을 사용하지 않음
        return null;
    }

    /**
     * Principal를 반환하는 메서드
     *
     * @return AuthUser 객체
     */
    @Override
    public AuthUser getPrincipal() {
        return authUser;
    }
}
