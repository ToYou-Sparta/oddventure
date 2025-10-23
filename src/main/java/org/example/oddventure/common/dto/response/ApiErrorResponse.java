package org.example.oddventure.common.dto.response;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.oddventure.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiErrorResponse {

    private final String httpStatus;
    private final int code;
    private final boolean success;
    private final String message;
    private final Object data;
    private final LocalDateTime timestamp;
    private final String requestUrl;

    private ApiErrorResponse(HttpStatus status, String message, String requestUrl) {
        this.httpStatus = status.name();
        this.code = status.value();
        this.success = false;
        this.message = message;
        this.data = null;
        this.timestamp = LocalDateTime.now();
        this.requestUrl = requestUrl;
    }

    public static ApiErrorResponse from(ErrorCode errorCode, HttpServletRequest request) {
        return new ApiErrorResponse(errorCode.getHttpStatus(),
                errorCode.getMessage(),
                request.getRequestURI());
    }

    public static ApiErrorResponse from(HttpStatus httpStatus, String message, HttpServletRequest request) {
        return new ApiErrorResponse(
                httpStatus,
                message,
                request.getRequestURI()
        );
    }
}