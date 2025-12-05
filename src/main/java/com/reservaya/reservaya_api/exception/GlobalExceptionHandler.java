package com.reservaya.reservaya_api.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleBadCredentialsException(
    BadCredentialsException ex
  ) {
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(
        Map.of(
          "message",
          "Credenciales incorrectas. Por favor, verifica tu correo y contraseña."
        )
      );
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, String>> handleIllegalStateException(
    IllegalStateException ex
  ) {
    return ResponseEntity
      .status(HttpStatus.CONFLICT) 
      .body(Map.of("message", ex.getMessage()));
  }
}
