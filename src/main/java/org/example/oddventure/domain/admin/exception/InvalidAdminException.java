package org.example.oddventure.domain.admin.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class InvalidAdminException extends GlobalException {
    public InvalidAdminException(ErrorCode errorCode) {
        super(errorCode);
    }
}
