package com.demo.security.spring.utils;

import com.demo.security.spring.model.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Log4j2
public class SecurityUtils {

  /** Max number of failed attempts allowed within a given time window or in a row before lockout */
  public static final int MAX_ALLOWED_FAILED_LOGIN_ATTEMPTS = 5;

  /**
   * Returns the currently logged in user or null if there is none.
   * @return the current user
   */
  public SecurityUser getCurrentUser() {
    SecurityContext context = SecurityContextHolder.getContext();
    if (context != null && context.getAuthentication() != null) {
      Object details = context.getAuthentication().getDetails();
      if (details instanceof SecurityUser) {
        return (SecurityUser) details;
      } else {
        log.error(() -> "Security context authentication contained unexpected details type " + details + "\nauth: " + context.getAuthentication());
      }
    }
    log.info(() -> "No authentication is currently present - user is not logged in");
    return null;
  }

  /**
   * Returns the current servlet request ( if exists ) from {@link RequestContextHolder}
   * @return the current servlet request or null
   */
  public static HttpServletRequest getCurrentRequest() {
    try {
      RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
      if (requestAttributes instanceof ServletRequestAttributes) {
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        return request;
      }
    } catch (Exception e) {
      log.error("Failed to retrieve current request", e);
    }
    log.warn(() -> "No current http request in context");
    return null;
  }

}
