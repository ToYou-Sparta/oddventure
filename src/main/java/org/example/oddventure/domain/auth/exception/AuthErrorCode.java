package org.example.oddventure.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 토큰 관련
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    TOKEN_UNSUPPORTED(HttpStatus.BAD_REQUEST, "지원되지 않는 JWT 형식입니다."),
    TOKEN_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JWT 처리 중 서버 오류가 발생했습니다."),
    TOKEN_NOT_FOUND(HttpStatus.BAD_REQUEST, "JWT 토큰을 찾을 수 없습니다."),
    ACCESS_TOKEN_MISMATCH(HttpStatus.FORBIDDEN, "액세스 토큰이 사용자 정보와 일치하지 않습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.FORBIDDEN, "리프레시 토큰이 일치하지 않습니다."),

    // 사용자 인증 관련
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
