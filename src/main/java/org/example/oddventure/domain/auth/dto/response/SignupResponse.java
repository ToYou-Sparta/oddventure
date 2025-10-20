package org.example.oddventure.domain.auth.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.oddventure.domain.user.entity.User;
import org.example.oddventure.domain.user.enums.UserRole;

public record SignupResponse(
        Long userId,
        String username,
        String email,
        UserRole role,
        BigDecimal point,
        LocalDateTime createdAt
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUserRole(),
                user.getPoint(),
                user.getCreatedAt()
        );
    }
}
