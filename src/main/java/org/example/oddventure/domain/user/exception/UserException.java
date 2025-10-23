package org.example.oddventure.domain.user.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class UserException extends GlobalException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
