package by.michael.api;

import by.michael.api.dto.ApiError;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(ApiError.of(ex.getMessage()));
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<ApiError> handleIo(IOException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ApiError.of("Ошибка чтения изображения: " + ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiError.of("Внутренняя ошибка сервера"));
  }
}
