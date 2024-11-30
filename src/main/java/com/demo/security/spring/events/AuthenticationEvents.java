package com.demo.security.spring.events;

import com.demo.security.spring.authentication.AuthenticationAttemptManager;
import com.demo.security.spring.model.AuthenticationFailureReason;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.service.SecurityUserService;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationFailureCredentialsExpiredEvent;
import org.springframework.security.authentication.event.AuthenticationFailureDisabledEvent;
import org.springframework.security.authentication.event.AuthenticationFailureExpiredEvent;
import org.springframework.security.authentication.event.AuthenticationFailureLockedEvent;
import org.springframework.security.authentication.event.AuthenticationFailureServiceExceptionEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Some demo authentication event listeners which create an auditable record of failed and successful
 * authentication events
 */
@Log4j2
@Builder
public class AuthenticationEvents {

  private AuthenticationAttemptManager authenticationAttemptManager;

  private SecurityUserService userService;

  @EventListener
  public void onSuccess(AuthenticationSuccessEvent event) {
    if (event.getAuthentication() != null) {
      log.info(() -> "Login successful for user " + event.getAuthentication().getName());
      String username = event.getAuthentication().getName();
      authenticationAttemptManager.handleSuccessfulAuthentication(username, getForUsername(username));
    } else {
      log.error(() -> "Login successful but event authentication is null " + event);
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
  public void onLocked(AuthenticationFailureLockedEvent event) {
    handleAuthenticationFailureEvent(event, AuthenticationFailureReason.LOCKED);
  }

  @EventListener
  public void onAccountExpired(AuthenticationFailureExpiredEvent event) {
    handleAuthenticationFailureEvent(event, AuthenticationFailureReason.ACCOUNT_EXPIRED);
  }

  @EventListener
  public void onCredentialsExpired(AuthenticationFailureCredentialsExpiredEvent event) {
    handleAuthenticationFailureEvent(event, AuthenticationFailureReason.CREDENTIALS_EXPIRED);
  }

  @EventListener
  public void onDisabled(AuthenticationFailureDisabledEvent event) {
    handleAuthenticationFailureEvent(event, AuthenticationFailureReason.DISABLED);
  }

  @EventListener
  public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
    handleAuthenticationFailureEvent(event, AuthenticationFailureReason.BAD_CREDENTIALS);
  }

  @EventListener
  public void onAuthenticationServiceException(AuthenticationFailureServiceExceptionEvent event) {
    handleAuthenticationFailureEvent(event, AuthenticationFailureReason.SERVICE_EXCEPTION);
  }

  /**
   * Generic handling for authentication failure events of various types which persists data about
   * the failed authentication event
   * @param event
   * @param reason
   */
  private void handleAuthenticationFailureEvent(AbstractAuthenticationFailureEvent event,
      AuthenticationFailureReason reason) {
    String username = getUsernameForEvent(event);
    if (StringUtils.isNotBlank(username)) {
      SecurityUser user = getForUsername(username);
      if (user != null && reason == AuthenticationFailureReason.BAD_CREDENTIALS) {
        userService.incrementFailedLogons(user);
      }
      authenticationAttemptManager.handleFailedAuthentication(username, user, reason);
    }
  }

  /**
   * Return the username from an implementation of {@link AbstractAuthenticationFailureEvent}
   * @param event
   * @return username or null
   */
  private String getUsernameForEvent(AbstractAuthenticationFailureEvent event) {
    String name = null;
    if (event.getAuthentication() != null) {
      name = event.getAuthentication().getName();
      log.info(() -> "Auth failure for username in authentication object " + event.getAuthentication().getName());
    } else if (event.getSource() != null &&
        event.getSource() instanceof UsernamePasswordAuthenticationToken token) {
      log.info(() -> "Auth failure for username found in token " + event.getAuthentication().getName());
      name = token.getName();
    }
    return name;
  }

  /**
   * Helper method to wrap and handle the UsernameNotFoundException which is thrown by the securityUserService
   * @param username
   * @return the located SecurityUser or null
   */
  private SecurityUser getForUsername(String username) {
    if (StringUtils.isNotBlank(username)) {
      try {
        return userService.loadUserByUsername(username);
      } catch (UsernameNotFoundException e) {
        final String tempUsername = username;
        log.info(() -> "User matching username " + tempUsername + " does not exist. Not an issue in this circumstance.");
      }
    }
    return null;
  }

}
