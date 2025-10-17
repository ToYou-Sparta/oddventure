package org.example.oddventure.domain.auth.dto;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.example.oddventure.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Getter
public class AuthUser {

    private final Long id;
    private final String username;
    private final String email;
    private final UserRole userRole;

    public AuthUser(Long id, String username, String email, UserRole userRole) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.userRole = userRole;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.getUserRole()));
    }
}
