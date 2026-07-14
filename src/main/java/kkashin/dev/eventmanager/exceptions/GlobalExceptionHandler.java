package kkashin.dev.eventmanager.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import kkashin.dev.eventmanager.exceptions.models.EMBadRequestException;
import kkashin.dev.eventmanager.exceptions.models.EMNotFoundException;
import kkashin.dev.eventmanager.exceptions.models.EMUnauthorizedRequestException;
import kkashin.dev.eventmanager.model.dto.HttpExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpExceptionDto> handleGlobalException(RuntimeException e) {
        var body = new HttpExceptionDto("Unexpected error occurred", e.getMessage(), LocalDateTime.now());
        return ResponseEntity.internalServerError().body(body);
    }

    @ExceptionHandler(EMBadRequestException.class)
    public ResponseEntity<HttpExceptionDto> handleBadRequestException(EMBadRequestException e) {
        var body = new HttpExceptionDto("Bad request", e.getMessage(), LocalDateTime.now());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(EMNotFoundException.class)
    public ResponseEntity<HttpExceptionDto> handleNotFoundException(EMNotFoundException e) {
        var body = new HttpExceptionDto("Resource not found", e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(EMUnauthorizedRequestException.class)
    public ResponseEntity<HttpExceptionDto> handleUnauthorizedException(EMUnauthorizedRequestException e) {
        var body = new HttpExceptionDto("Not authorized", e.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
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

    private String formatViolation(ConstraintViolation<?> violation) {
        var path = violation.getPropertyPath().toString();
        var field = path.substring(path.lastIndexOf('.') + 1);

        return "%s: %s".formatted(field, violation.getMessage());
    }
}
