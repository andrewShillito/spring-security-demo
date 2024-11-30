package com.demo.security.spring.authentication;

import com.demo.security.spring.model.AuthenticationAttempt;
import com.demo.security.spring.model.AuthenticationFailureReason;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.AuthenticationAttemptRepository;
import com.demo.security.spring.utils.SecurityUtils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
public class AuthenticationAttemptManager {

  private AuthenticationAttemptRepository attemptRepository;

  public AuthenticationAttemptManager setAttemptRepository(AuthenticationAttemptRepository attemptRepository) {
    if (attemptRepository == null) {
      throw new IllegalArgumentException(AuthenticationAttemptRepository.class.getName() + " cannot be null");
    }
    this.attemptRepository = attemptRepository;
    return this;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void handleFailedAuthentication(String username, SecurityUser user, @NonNull AuthenticationFailureReason reason) {
    if (user == null && reason != AuthenticationFailureReason.USER_NOT_FOUND) {
      log.warn(() -> "User for authentication attempt record is null");
    }
    if (user == null) {
      persist(buildFailedAttempt(username, reason));
    } else {
      persist(buildFailedAttempt(username, user, reason));
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void handleSuccessfulAuthentication(String username, SecurityUser user) {
    if (StringUtils.isBlank(username) && user == null) {
      log.error(() -> "Unable to create authentication attempt record as username and user are null and blank");
    } else {
      persist(buildSuccessfulAttempt(username, user));
    }
  }

  private AuthenticationAttempt buildFailedAttempt(String username, SecurityUser user, AuthenticationFailureReason failureReason) {
    if (user == null) {
      return buildFailedAttempt(username, failureReason);
    } else {
      return AuthenticationAttempt.builder()
          .fromUser(user)
          .fromRequest(SecurityUtils.getCurrentRequest())
          .failureReason(failureReason)
          .now()
          .success(false)
          .build();
    }
  }

  private AuthenticationAttempt buildSuccessfulAttempt(String username, SecurityUser user) {
    if (user == null) {
      return AuthenticationAttempt.builder()
          .username(username)
          .fromRequest(SecurityUtils.getCurrentRequest())
          .now()
          .success(true)
          .build();
    } else {
      return AuthenticationAttempt.builder()
          .fromUser(user)
          .fromRequest(SecurityUtils.getCurrentRequest())
          .now()
          .success(true)
          .build();
    }
  }

  private AuthenticationAttempt buildFailedAttempt(String username, AuthenticationFailureReason failureReason) {
    return AuthenticationAttempt.builder()
        .username(username)
        .fromRequest(SecurityUtils.getCurrentRequest())
        .failureReason(failureReason)
        .now()
        .success(false)
        .build();
  }

  private void persist(@NonNull AuthenticationAttempt attempt) {
    try {
      attemptRepository.save(attempt);
    } catch (Exception e) {
      // failing to persist the authentication attempt is not fatal
      log.error(() -> "Failed to persist authentication attempt details", e);
    }
  }

}
