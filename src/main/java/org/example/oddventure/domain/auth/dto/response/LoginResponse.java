package org.example.oddventure.domain.auth.dto.response;

public record LoginResponse(
        String access_token,
        String refresh_token
) {
}
