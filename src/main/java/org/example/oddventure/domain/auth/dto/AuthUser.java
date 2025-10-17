package org.example.oddventure.domain.auth.dto;

import java.util.Collection;
import java.util.List;
import org.example.oddventure.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AuthUser(
        Long id,
        String username,
        String email,
        UserRole userRole) {

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.getUserRole()));
    }
}
