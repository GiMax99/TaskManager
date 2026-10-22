package com.gimax.taskmanager.exception;

import com.gimax.taskmanager.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальная обработка исключений API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
        LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        log.info("Resource not found: method={}, path={}", request.getMethod(), request.getRequestURI());

        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException exception, HttpServletRequest request) {
        log.warn("Duplicate resource: method={}, path={}", request.getMethod(), request.getRequestURI());

        return build(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Data integrity violation: method={}, path={}, message={}", request.getMethod(), request.getRequestURI(),
            exception.getMessage());

        return build(HttpStatus.CONFLICT, "Нарушено ограничение целостности данных", request);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationCredentialsNotFoundException exception,
                                                              HttpServletRequest request) {
        log.warn("Authentication required: method={}, path={}, message={}", request.getMethod(), request.getRequestURI(),
            exception.getMessage());

        return build(HttpStatus.UNAUTHORIZED, "Требуется аутентификация", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
        log.warn("Authentication failed: method={}, path={}, exceptionType={}", request.getMethod(), request.getRequestURI(),
            exception.getClass().getSimpleName());

        return build(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception, HttpServletRequest request) {
        log.warn("Access denied: method={}, path={}", request.getMethod(), request.getRequestURI());

        return build(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        log.debug("Request validation failed: method={}, path={}, errors={}", request.getMethod(), request.getRequestURI(), message);

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        String message = exception.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining("; "));
        log.debug("Constraint violation: method={}, path={}, errors={}", request.getMethod(), request.getRequestURI(), message);

        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        log.debug("Unreadable request body: method={}, path={}, message={}", request.getMethod(), request.getRequestURI(),
            exception.getMessage());

        return build(HttpStatus.BAD_REQUEST, "Некорректное тело запроса", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected exception: method={}, path={}", request.getMethod(), request.getRequestURI(), exception);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
