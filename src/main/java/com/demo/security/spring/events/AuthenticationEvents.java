package com.demo.security.spring.events;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;

/**
 * Sme demo authentication event listeners which log info
 */
@Log4j2
public class AuthenticationEvents {

  @EventListener
  public void onSuccess(AuthenticationSuccessEvent event) {
    if (event.getAuthentication() != null) {
      log.info(() -> "Login successful for user " + event.getAuthentication().getName());
    } else {
      log.error(() -> "Login successful " + event);
    }
  }

  @EventListener
  public void onLogout(LogoutSuccessEvent logoutEvent) {
    if (logoutEvent.getAuthentication() != null) {
      log.info(() -> "Logout successful for user " + logoutEvent.getAuthentication().getName());
    } else {
      log.info(() -> "Logout successful " + logoutEvent);
    }
  }

  @EventListener
  public void onBadCredentials(AuthenticationFailureBadCredentialsEvent badCredentialsEvent) {
    if (badCredentialsEvent.getAuthentication() != null) {
      log.info(() -> "Login failed due to bad credentials for user " + badCredentialsEvent.getAuthentication().getName());
    } else {
      log.info(() -> "Login failed due to bad credentials " + badCredentialsEvent);
    }
  }

}
