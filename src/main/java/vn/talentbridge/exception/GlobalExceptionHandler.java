package vn.talentbridge.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.talentbridge.common.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(vn.talentbridge.core.domain.exception.DomainException.class)
    public ResponseEntity<ApiResponse<Object>> handleDomainException(vn.talentbridge.core.domain.exception.DomainException ex) {
        log.warn("DomainException occurred: code={}, message={}", ex.getCode(), ex.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getCode() == 40401) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex.getCode() == 40901) {
            status = HttpStatus.CONFLICT;
        } else if (ex.getCode() == 40102) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (ex.getCode() == 40301) {
            status = HttpStatus.FORBIDDEN;
        }
        ApiResponse<Object> response = ApiResponse.error(ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException ex) {
        log.warn("AppException occurred: code={}, message={}", ex.getErrorCode().getCode(), ex.getMessage());
        ErrorCode ec = ex.getErrorCode();
        ApiResponse<Object> response = ApiResponse.error(ec.getCode(), ex.getMessage());
        return new ResponseEntity<>(response, ec.getHttpStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("AccessDeniedException occurred: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(
                ErrorCode.FORBIDDEN.getCode(),
                ErrorCode.FORBIDDEN.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Validation error: {}", errors);
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .statusCode(ErrorCode.VALIDATION_FAILED.getCode())
                .message("Dữ liệu đầu vào không hợp lệ")
                .data(errors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        ApiResponse<Object> response = ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}