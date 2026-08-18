package kkashin.dev.eventmanager.exceptions;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import kkashin.dev.eventmanager.exceptions.models.ManagerBadRequestException;
import kkashin.dev.eventmanager.exceptions.models.ManagerNotFoundException;
import kkashin.dev.eventmanager.exceptions.models.ManagerUnauthorizedRequestException;
import kkashin.dev.eventmanager.model.dto.HttpExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<HttpExceptionDto> handleAuthenticationException(AuthenticationException e) {
        var body = new HttpExceptionDto("Not authorized", "Invalid login or password", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpExceptionDto> handleAccessDeniedException(AccessDeniedException e) {
        var body = new HttpExceptionDto("Forbidden", e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(ManagerBadRequestException.class)
    public ResponseEntity<HttpExceptionDto> handleBadRequestException(ManagerBadRequestException e) {
        var body = new HttpExceptionDto("Bad request", e.getMessage(), LocalDateTime.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(value = {ManagerNotFoundException.class, EntityNotFoundException.class})
    public ResponseEntity<HttpExceptionDto> handleNotFoundException(RuntimeException e) {
        var body = new HttpExceptionDto("Resource not found", e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ManagerUnauthorizedRequestException.class)
    public ResponseEntity<HttpExceptionDto> handleUnauthorizedException(ManagerUnauthorizedRequestException e) {
        var body = new HttpExceptionDto("Not authorized", e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpExceptionDto> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        var message = exception.getBindingResult().getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(fieldError -> "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        var body = new HttpExceptionDto(
                "Validation failed",
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<HttpExceptionDto> handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        var message = exception.getConstraintViolations()
                .stream()
                .sorted(Comparator.comparing(
                        violation -> violation.getPropertyPath().toString()
                ))
                .map(this::formatViolation)
                .collect(Collectors.joining("; "));

        var body = new HttpExceptionDto(
                "Validation failed",
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpExceptionDto> handleGlobalException(RuntimeException e) {
        var body = new HttpExceptionDto("Unexpected error occurred", e.getMessage(), LocalDateTime.now());
        return ResponseEntity.internalServerError().body(body);
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        var path = violation.getPropertyPath().toString();
        var field = path.substring(path.lastIndexOf('.') + 1);

        return "%s: %s".formatted(field, violation.getMessage());
    }
}
