package org.example.oddventure.domain.auth.dto.response;

public record AccessTokenResponse(
        String accessToken
) {
    public static AccessTokenResponse of(String accessToken) {
        return new AccessTokenResponse(accessToken);
    }
}