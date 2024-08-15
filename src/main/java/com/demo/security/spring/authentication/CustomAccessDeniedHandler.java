package com.demo.security.spring.authentication;

import com.demo.security.spring.controller.error.AuthErrorDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  private final boolean isProd;

  public CustomAccessDeniedHandler(ObjectMapper objectMapper, boolean isProd) {
    Preconditions.checkNotNull(objectMapper, "Object mapper cannot be null");
    this.objectMapper = objectMapper;
    this.isProd = isProd;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    if (response != null && response.isCommitted()) {
      return;
    }
    response.setHeader("example-failed-authorization-header", "Example extra header value");
    response.setStatus(HttpStatus.FORBIDDEN.value());
    writeResponseBody(request, response, accessDeniedException);
  }

  protected void writeResponseBody(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AccessDeniedException accessDeniedException) {
    try {
      objectMapper.writerWithDefaultPrettyPrinter()
          .writeValue(
              response.getOutputStream(),
              AuthErrorDetailsResponse.builder()
                  .time(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))
                  .errorCode(HttpStatus.FORBIDDEN.value())
                  .errorMessage(!isProd && accessDeniedException != null ? accessDeniedException.getMessage() : HttpStatus.FORBIDDEN.getReasonPhrase())
                  .requestUri(request != null ? request.getRequestURI() : null)
                  .additionalInfo("Example additional info")
                  .build());
    } catch (Exception e) {
      throw new RuntimeException("Failed to produce AuthErrorDetailsResponse with error ", e);
    }
  }
}
