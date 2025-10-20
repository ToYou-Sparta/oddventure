package org.example.oddventure.domain.team.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TeamErrorCode implements ErrorCode {

    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 팀 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
