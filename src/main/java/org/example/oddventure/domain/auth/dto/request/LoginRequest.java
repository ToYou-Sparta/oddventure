package org.example.oddventure.domain.auth.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}
