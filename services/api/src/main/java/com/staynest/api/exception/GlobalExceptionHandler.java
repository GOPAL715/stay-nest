package com.staynest.api.exception;

import com.staynest.api.util.ApiErrorResponse;
import com.staynest.api.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler({BusinessRuleException.class, ConflictException.class})
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(
            ApplicationException ex, HttpServletRequest request) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler({AuthorizationException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            Exception ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You do not have permission to perform this action",
                        request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiErrorResponse.FieldValidationError> fieldErrors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> ApiErrorResponse.FieldValidationError.builder()
                        .field(((FieldError) error).getField())
                        .message(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());
        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.validationError(request.getRequestURI(), fieldErrors));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplication(
            ApplicationException ex, HttpServletRequest request) {
        log.error("Application error [{}]: {}", ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Resource not found: " + ex.getResourcePath(),
                        request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(
            Exception ex, HttpServletRequest request) {
        log.error("━━━ UNHANDLED EXCEPTION ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.error("  Path    : {}", request.getRequestURI());
        log.error("  Method  : {}", request.getMethod());
        log.error("  Type    : {}", ex.getClass().getName());
        log.error("  Message : {}", ex.getMessage());
        log.error("  Cause   : {}", ex.getCause() != null ? ex.getCause().getMessage() : "none");
        log.error("  Trace   :", ex);
        log.error("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later.",
                        request.getRequestURI()));
    }
}
