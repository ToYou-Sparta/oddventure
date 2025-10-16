package org.example.oddventure.domain.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    JWT_CANNOT_FIND_TOKEN(HttpStatus.NOT_FOUND, "JWT 토큰을 찾을 수 없습니다.");

    // private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
