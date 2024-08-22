package com.demo.security.spring.authentication;

import com.demo.security.spring.model.AuthenticationAttempt;
import com.demo.security.spring.model.AuthenticationFailureReason;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.AuthenticationAttemptRepository;
import com.demo.security.spring.utils.SecurityUtils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
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
      log.error(() -> "User is unexpectedly null!");
    }
    switch (reason) {
      case ACCOUNT_EXPIRED -> handleAccountExpired(username, user);
      case BAD_CREDENTIALS -> handleBadCredentials(username, user);
      case CREDENTIALS_EXPIRED -> handleCredentialsExpired(username, user);
      case DISABLED -> handleDisabled(username, user);
      case LOCKED -> handleLocked(username, user);
      case NO_AUTHORITIES -> handleNoAuthorities(username, user);
      case USER_NOT_FOUND -> handleUserNotFound(username);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void handleSuccessfulAuthentication(SecurityUser user) {
    if (user == null) {
      log.error(() -> "Cannot record successful authentication due to null user");
    } else {
      persist(buildSuccessfulAttempt(user));
    }
  }

  private void handleAccountExpired(String username, final SecurityUser user) {
    persist(buildFailedAttempt(username, user, AuthenticationFailureReason.ACCOUNT_EXPIRED));
  }

  private void handleBadCredentials(String username, final SecurityUser user) {
    persist(buildFailedAttempt(username, user, AuthenticationFailureReason.BAD_CREDENTIALS));
  }

  private void handleCredentialsExpired(String username, final SecurityUser user) {
    persist(buildFailedAttempt(username, user, AuthenticationFailureReason.CREDENTIALS_EXPIRED));
  }

  private void handleDisabled(String username, final SecurityUser user) {
    persist(buildFailedAttempt(username, user, AuthenticationFailureReason.DISABLED));
  }

  protected void handleLocked(String username, final SecurityUser user) {
    persist(buildFailedAttempt(username, user, AuthenticationFailureReason.LOCKED));
  }

  private void handleNoAuthorities(String username, final SecurityUser user) {
    persist(buildFailedAttempt(username, user, AuthenticationFailureReason.NO_AUTHORITIES));
  }

  private void handleUserNotFound(String username) {
    persist(buildFailedAttempt(username, AuthenticationFailureReason.USER_NOT_FOUND));
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

  private AuthenticationAttempt buildSuccessfulAttempt(SecurityUser user) {
    return AuthenticationAttempt.builder()
        .fromUser(user)
        .fromRequest(SecurityUtils.getCurrentRequest())
        .now()
        .success(true)
        .build();
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
