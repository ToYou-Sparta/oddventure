package org.example.oddventure.domain.auth.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SignupResponse(
        Long user_id,
        String username,
        String email,
        String role,
        BigDecimal point,
        LocalDateTime created_at
) {
}
