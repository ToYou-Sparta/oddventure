package org.example.oddventure.domain.auth.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class AuthException extends GlobalException {
    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }
}
