package com.andrewliu.gamesession.exception;

import com.andrewliu.gamesession.dto.ErrorResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(ErrorResponse.validation(errors));
  }

  @ExceptionHandler(BadRequestException.class)
  ResponseEntity<ErrorResponse> badRequest(BadRequestException ex) {
    return response(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  ResponseEntity<ErrorResponse> notFound(NotFoundException ex) {
    return response(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ErrorResponse> conflict(ConflictException ex) {
    return response(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  ResponseEntity<ErrorResponse> unauthorized(UnauthorizedException ex) {
    return response(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  ResponseEntity<ErrorResponse> illegalState(IllegalStateException ex) {
    return response(HttpStatus.CONFLICT, ex.getMessage());
  }

  private ResponseEntity<ErrorResponse> response(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(status.value(), status.getReasonPhrase(), message));
  }
}
