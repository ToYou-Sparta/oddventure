package org.example.oddventure.domain.user.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class InvalidUserException extends GlobalException {
    public InvalidUserException(ErrorCode errorCode) {
        super(errorCode);
    }
}