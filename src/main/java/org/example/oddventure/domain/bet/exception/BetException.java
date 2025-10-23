package org.example.oddventure.domain.bet.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class BetException extends GlobalException {
    public BetException(ErrorCode errorCode) {
        super(errorCode);
    }
}
