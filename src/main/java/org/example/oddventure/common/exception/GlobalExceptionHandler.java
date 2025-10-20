package org.example.oddventure.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.common.dto.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(GlobalException ex, HttpServletRequest request) {
        log.error("비즈니스 오류 발생 ", ex);
        return handleExceptionInternal(ex.getErrorCode(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                                  HttpServletRequest request) {
        log.warn("유효성 검사 실패 ", ex);

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .findFirst()
                .orElse("유효성 검사에 실패했습니다.");

        return handleExceptionInternal(HttpStatus.BAD_REQUEST, errorMessage, request);
    }

    private ResponseEntity<ApiErrorResponse> handleExceptionInternal(ErrorCode errorCode, HttpServletRequest request) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiErrorResponse.from(errorCode, request));
    }

    private ResponseEntity<ApiErrorResponse> handleExceptionInternal(HttpStatus httpStatus,
                                                                     String message,
                                                                     HttpServletRequest request) {
        return ResponseEntity
                .status(httpStatus)
                .body(ApiErrorResponse.from(httpStatus, message, request));
    }
}
