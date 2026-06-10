package com.andrewliu.gamesession.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    Map<String, String> validationErrors) {

  public static ErrorResponse of(int status, String error, String message) {
    return new ErrorResponse(Instant.now(), status, error, message, Map.of());
  }

  public static ErrorResponse validation(Map<String, String> errors) {
    return new ErrorResponse(Instant.now(), 400, "Bad Request", "Validation failed", errors);
  }
}
