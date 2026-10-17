package com.cricket.cms.exception;

import com.cricket.cms.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidMatchStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidMatchStateException ex,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
