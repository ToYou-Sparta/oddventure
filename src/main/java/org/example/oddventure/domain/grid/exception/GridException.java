package org.example.oddventure.domain.grid.exception;

import org.example.oddventure.common.exception.ErrorCode;
import org.example.oddventure.common.exception.GlobalException;

public class GridException extends GlobalException {
    public GridException(ErrorCode errorCode) {
        super(errorCode);
    }
}
