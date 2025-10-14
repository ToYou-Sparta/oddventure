package org.example.oddventure.domain.auth.dto.request;

public record SignUpRequest(
        String username,
        String email,
        String password
) {
}
