package org.example.oddventure.domain.team.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class TeamException extends GlobalException {
    public TeamException(ErrorCode errorCode) {
        super(errorCode);
    }
}
