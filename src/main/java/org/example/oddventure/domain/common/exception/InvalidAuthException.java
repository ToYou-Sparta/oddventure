package org.example.oddventure.domain.common.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class InvalidAuthException extends GlobalException {
    public InvalidAuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
