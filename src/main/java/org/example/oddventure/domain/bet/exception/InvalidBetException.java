package org.example.oddventure.domain.bet.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class InvalidBetException extends GlobalException {
    public InvalidBetException(ErrorCode errorCode) {
        super(errorCode);
    }
}
