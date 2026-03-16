package com.nilesh.cym.exception;

import com.nilesh.cym.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> details = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .toList();
        log.warn("request_failed type=validation path={} status={} details={}",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                details);

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", details, request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();
        log.warn("request_failed type=constraint_violation path={} status={} details={}",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(),
                details);

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", details, request.getRequestURI());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {
        HttpStatusCode code = ex.getStatusCode();
        HttpStatus status = HttpStatus.valueOf(code.value());
        log.warn("request_failed type=response_status path={} status={} reason={}",
                request.getRequestURI(),
                status.value(),
                ex.getReason());

        return buildResponse(
                status,
                ex.getReason() == null ? status.getReasonPhrase() : ex.getReason(),
                Collections.emptyList(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler({BadCredentialsException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleSecurity(
            Exception ex,
            HttpServletRequest request
    ) {
        HttpStatus status = ex instanceof BadCredentialsException ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        log.warn("request_failed type=security path={} status={} reason={}",
                request.getRequestURI(),
                status.value(),
                ex.getMessage());
        return buildResponse(status, ex.getMessage(), Collections.emptyList(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("request_failed type=unhandled path={} status={} exception={}",
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getClass().getSimpleName(),
                ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                List.of(ex.getClass().getSimpleName()),
                request.getRequestURI()
        );
    }

    private ResponseEntity<ApiResponse<Void>> buildResponse(
            HttpStatus status,
            String message,
            List<String> details,
            String path
    ) {
        List<String> errors = details == null || details.isEmpty() ? List.of("path: " + path) : details;
        ApiResponse<Void> error = ApiResponse.error(message, errors);
        return ResponseEntity.status(status).body(error);
    }
}
