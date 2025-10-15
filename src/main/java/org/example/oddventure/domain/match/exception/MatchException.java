package org.example.oddventure.domain.match.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class MatchException extends GlobalException {
    public MatchException(ErrorCode errorCode) {
        super(errorCode);
    }
}
