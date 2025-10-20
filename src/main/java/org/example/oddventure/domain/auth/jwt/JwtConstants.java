package org.example.oddventure.domain.auth.jwt;

public class JwtConstants {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 1000L; // 60분
    public static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7일
    public static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
}
