package org.example.oddventure.domain.admin.dto.response;

import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.enums.UserRole;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserAdminResponse(
        Long userId,
        String username,
        String email,
        BigDecimal point,
        UserRole role,
        LocalDateTime createdAt
) {
    public static UserAdminResponse fromEntity(User user) {
        return new UserAdminResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPoint(),
                user.getUserRole(),
                user.getCreatedAt()
        );
    }
}