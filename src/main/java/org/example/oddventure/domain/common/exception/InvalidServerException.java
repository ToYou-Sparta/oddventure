package org.example.oddventure.domain.common.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class InvalidServerException extends GlobalException {
    public InvalidServerException(ErrorCode errorCode) {
        super(errorCode);
    }
}
