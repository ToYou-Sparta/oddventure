package org.example.oddventure.common.config;

import org.example.oddventure.domain.common.dto.AuthUser;
import org.springframework.security.authentication.AbstractAuthenticationToken;

// AuthUser를 Spring Security에서 사용하기 위한 작업을 하는 클래스
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthUser authUser;

    /**
     * AuthUser를 받아 인증 토큰을 만드는 메서드
     * @param authUser 인증된 사용자 정보
     */
    public JwtAuthenticationToken(AuthUser authUser) {
        super(authUser.getAuthorities());
        this.authUser = authUser;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    /**
     * Principal를 반환하는 메서드
     *
     * @return AuthUser 객체
     */
    @Override
    public Object getPrincipal() {
        return authUser;
    }
}
