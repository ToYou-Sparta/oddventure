package org.example.oddventure.domain.grid.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GridErrorCode implements ErrorCode {

    RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "응답 정보를 찾을 수 없습니다."),
    FAIL_TO_SERIALIZE(HttpStatus.EXPECTATION_FAILED, "직렬화에 실패하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
