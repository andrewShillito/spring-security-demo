package com.demo.security.spring.events;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.authorization.event.AuthorizationEvent;
import org.springframework.security.authorization.event.AuthorizationGrantedEvent;
import org.springframework.security.core.Authentication;

/**
 * Placeholder example authorization event listeners for granted and denied authorization events
 * which log info.
 */
@Log4j2
public class AuthorizationEvents {

  @EventListener
  public void onFailure(AuthorizationDeniedEvent<?> deniedEvent) {
    log.info(toEventInfoString(deniedEvent));
  }

//  By default spring does not publish AuthorizationGrantedEvents due to the
//  amount of noise it can generate. Related documentation is at
//  https://docs.spring.io/spring-security/reference/servlet/authorization/events.html
//  @EventListener
//  public void onSuccess(AuthorizationGrantedEvent<?> grantedEvent) {
//    log.info(toEventInfoString(grantedEvent));
//  }

  /**
   * Generates a simple information string for an AuthorizationEvent to log out
   * @param event the granted, denied, or other implementing AuthorizationEvent type
   * @return an information string about the event
   */
  private String toEventInfoString(AuthorizationEvent event) {
    final StringBuilder sb = new StringBuilder();
    if (event instanceof AuthorizationGrantedEvent<?>) {
      sb.append("Authorization granted for");
    } else if (event instanceof AuthorizationDeniedEvent<?>) {
      sb.append("Authorization denied for");
    } else {
      sb.append("Authorization event with type '%s' for".formatted(event != null ? event.getClass().getName() : "null"));
    }
    if (event != null) {
      Authentication authentication = event.getAuthentication() != null ? event.getAuthentication().get() : null;
      AuthorizationDecision authorizationDecision = event.getAuthorizationDecision();
      String username = authentication != null ? authentication.getName() : "unknown";
      sb.append(" user: '%s' due to '%s'".formatted(username, authorizationDecision));
    } else {
      sb.append(" null or unknown event");
    }
    return sb.toString();
  }



}
