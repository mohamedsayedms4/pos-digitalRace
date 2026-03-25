package com.example.pos.exception;

import com.example.pos.dto.ApiError;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /* ---------- 400 ---------- */

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, Locale locale) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /* ---------- 401 ---------- */

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, Locale locale) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, Locale locale) {
        String msg = messageSource.getMessage("auth.invalid.credentials", null, locale);
        return buildError(HttpStatus.UNAUTHORIZED, msg, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex, Locale locale) {
        String msg = messageSource.getMessage("auth.user.disabled", null, locale);
        return buildError(HttpStatus.UNAUTHORIZED, msg, null);
    }

    /* ---------- 403 ---------- */

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, Locale locale) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, Locale locale) {
        String msg = messageSource.getMessage("error.forbidden", null, locale);
        return buildError(HttpStatus.FORBIDDEN, msg, null);
    }

    /* ---------- 404 ---------- */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, Locale locale) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    /* ---------- 422 Validation ---------- */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, Locale locale) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        String msg = messageSource.getMessage("error.validation", null, locale);
        return buildError(HttpStatus.UNPROCESSABLE_ENTITY, msg, errors);
    }

    /* ---------- 500 ---------- */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, Locale locale) {
        String msg = messageSource.getMessage("error.server", null, locale);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, msg, null);
    }

    /* ---------- Helper ---------- */

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message, Map<String, String> errors) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .message(message)
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
