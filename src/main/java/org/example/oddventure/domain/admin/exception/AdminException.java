package org.example.oddventure.domain.admin.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class AdminException extends GlobalException {
    public AdminException(ErrorCode errorCode) {
        super(errorCode);
    }
}
