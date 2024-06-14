package com.demo.security.spring.controller.error;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class UserCreationException extends ResponseStatusException {

  public UserCreationException(HttpStatusCode status) {
    super(status);
  }

  public UserCreationException(HttpStatusCode status, String reason) {
    super(status, reason);
  }

  public UserCreationException(int rawStatusCode, String reason, Throwable cause) {
    super(rawStatusCode, reason, cause);
  }

  public UserCreationException(HttpStatusCode status, String reason, Throwable cause) {
    super(status, reason, cause);
  }

  protected UserCreationException(HttpStatusCode status, String reason, Throwable cause,
      String messageDetailCode, Object[] messageDetailArguments) {
    super(status, reason, cause, messageDetailCode, messageDetailArguments);
  }

  @Override
  public String getTypeMessageCode() {
    return super.getTypeMessageCode();
  }

  @Override
  public String getTitleMessageCode() {
    return super.getTitleMessageCode();
  }

  @Override
  public Object[] getDetailMessageArguments(MessageSource messageSource, Locale locale) {
    return super.getDetailMessageArguments(messageSource, locale);
  }
}
