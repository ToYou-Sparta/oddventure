package org.example.oddventure.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.oddventure.common.dto.response.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiErrorResponse> handleGlobalException(GlobalException ex, HttpServletRequest request) {
        log.warn("비즈니스 오류 발생 ", ex);
        return handleExceptionInternal(ex.getErrorCode(), request);
    }

    // DTO 유효성 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        log.warn("유효성 검사 실패 ", ex);

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .findFirst()
                .orElse(CommonErrorCode.VALIDATION_FAILED.getMessage());

        return handleExceptionInternal(CommonErrorCode.VALIDATION_FAILED, errorMessage, request);
    }

    // 잘못된 요청 관련 예외
    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class, // 지원되지 않는 HTTP 메서드 (GET 대신 POST)
            HttpMessageNotReadableException.class, // 요청 본문(JSON 등) 파싱 실패
            MissingServletRequestParameterException.class // 필수 요청 파라미터 누락
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("잘못된 요청", ex);
        return handleExceptionInternal(CommonErrorCode.INVALID_INPUT, request);
    }

    // 접근 권한 관련 예외
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("접근 권한 없음", ex);
        return handleExceptionInternal(CommonErrorCode.FORBIDDEN, request);
    }

    // 그 외 예기치 못한 모든 예외 (최하단에 위치해야함!)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error("서버 내부 오류 발생", ex);
        return handleExceptionInternal(CommonErrorCode.INTERNAL_SERVER_ERROR, request);
    }

    private ResponseEntity<ApiErrorResponse> handleExceptionInternal(ErrorCode errorCode, HttpServletRequest request) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiErrorResponse.from(errorCode, request));
    }

    private ResponseEntity<ApiErrorResponse> handleExceptionInternal(
            ErrorCode errorCode,
            String message,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiErrorResponse.from(errorCode.getHttpStatus(), message, request));
    }
}
