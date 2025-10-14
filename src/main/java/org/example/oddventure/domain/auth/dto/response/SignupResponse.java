package org.example.oddventure.domain.auth.dto.response;

import java.time.LocalDateTime;

public record SignupResponse(
        Long user_id,
        String username,
        String email,
        String role,
        Integer point,
        LocalDateTime created_at
) {
}
