package org.example.oddventure.domain.bet.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BetErrorCode implements ErrorCode {

    MATCH_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 매치입니다."),
    NOT_ENOUGH_POINTS(HttpStatus.BAD_REQUEST, "보유 포인트가 부족합니다."),
    MATCH_NOT_BETTABLE(HttpStatus.BAD_REQUEST, "베팅이 마감되었거나 진행 중인 매치입니다."),
    MATCH_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "취소가 불가능한 매치입니다."),
    ODDS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 베팅 항목입니다."),
    BET_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 베팅 내역입니다."),
    PERMISSION_DENIED(HttpStatus.FORBIDDEN, "베팅을 취소할 권한이 없습니다."),
    POINT_BATCH_EXCEPTION(HttpStatus.EXPECTATION_FAILED, "포인트 정산 배치 실행 오류");

    private final HttpStatus httpStatus;
    private final String message;
}