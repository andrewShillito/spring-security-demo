package com.demo.security.spring.utils;

import com.demo.security.spring.model.SecurityUser;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Log4j2
public class SecurityUtils {

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

}
