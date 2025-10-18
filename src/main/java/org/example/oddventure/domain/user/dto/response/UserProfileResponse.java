package org.example.oddventure.domain.user.dto.response;

import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.enums.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserProfileResponse(
        Long userId,
        String username,
        String email,
        BigDecimal point,
        UserRole role,
        LocalDateTime createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPoint(),
                user.getUserRole(),
                user.getCreatedAt()
        );
    }
}