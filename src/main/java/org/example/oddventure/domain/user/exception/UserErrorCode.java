package org.example.oddventure.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USR_INVALID_USER_ID( HttpStatus.BAD_REQUEST, "유효하지 않은 유저 ID 입니다."),
    USR_INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "유효하지 않은 유저 권한"),
    USR_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    ALREADY_EXIST_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다.");


    //private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}