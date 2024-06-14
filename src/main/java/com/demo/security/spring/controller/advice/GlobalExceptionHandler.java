package com.demo.security.spring.controller.advice;

import com.demo.security.spring.controller.error.UserCreationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(value = { NullPointerException.class })
  public ResponseEntity<Object> handleNullPointer(RuntimeException ex, WebRequest request) {
    return handleExceptionInternal(ex, "Server error", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  @ExceptionHandler(value = { UserCreationException.class })
  public ResponseEntity<Object> handleErrorResponseException(
      UserCreationException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    return super.handleErrorResponseException(ex, headers, status, request);
  }

}
