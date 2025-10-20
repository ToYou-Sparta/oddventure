package org.example.oddventure.domain.admin.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminErrorCode implements ErrorCode {
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 경기를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}