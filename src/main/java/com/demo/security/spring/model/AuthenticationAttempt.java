package com.demo.security.spring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Entity
@Table(name = "authentication_attempts", indexes = {
    @Index(name = "ix_authentication_attempts_username", columnList = "username,failure_reason"),
    @Index(name = "ix_authentication_attempts_user_id", columnList = "user_id,failure_reason"),
    @Index(name = "ix_authentication_attempts_resource_path_username", columnList = "requested_resource,username")
})
@Getter
@Setter
@Log4j2
@SequenceGenerator(name = "authentication_attempts_id_seq", sequenceName = "authentication_attempts_id_seq", allocationSize = 50, initialValue = 1)
public class AuthenticationAttempt {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authentication_attempts_id_seq")
  private Long id;

  @Column(name = "user_id")
  private Long userId;

  /**
   * Storing the username is not required for known users as we have the id.
   * However, storing the username provided by the user when we didn't find
   * an associated user can have value.
   *
   * Note that {@link ClientInfo#getRemoteUser()}} would also be the authenticated username if present
   */
  @Column(name = "username", length = 100)
  private String username;

  @Column(name = "attempt_time", nullable = false)
  private ZonedDateTime attemptTime;

  @Column(name = "successful", nullable = false)
  private boolean successful;

  @Column(name = "failure_reason", length = 50)
  @Enumerated(EnumType.STRING)
  private AuthenticationFailureReason failureReason;

  /**
   * The resource path from the request
   */
  @Column(name = "requested_resource", length = 200)
  private String requestedResource;

  @Embedded
  private ClientInfo clientInfo;

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private AuthenticationAttempt attempt = new AuthenticationAttempt();

    public AuthenticationAttempt build() {
      return attempt;
    }

    public Builder clear() {
      attempt = new AuthenticationAttempt();
      return this;
    }

    public Builder id(Long id) {
      attempt.setId(id);
      return this;
    }

    public Builder userId(Long userId) {
      attempt.setUserId(userId);
      return this;
    }

    public Builder username(String username) {
      attempt.setUsername(username);
      return this;
    }

    public Builder attemptTime(ZonedDateTime time) {
      attempt.setAttemptTime(time);
      return this;
    }

    public Builder now() {
      attempt.setAttemptTime(ZonedDateTime.now());
      return this;
    }

    public Builder success(boolean successful) {
      attempt.setSuccessful(successful);
      return this;
    }

    public Builder failureReason(AuthenticationFailureReason reason) {
      attempt.setFailureReason(reason);
      return this;
    }

    public Builder clientInfo(ClientInfo info) {
      attempt.setClientInfo(info);
      return this;
    }

    public Builder requestedResource(String resource) {
      attempt.setRequestedResource(resource);
      return this;
    }

    public Builder fromRequest(HttpServletRequest request) {
      if (request != null) {
        attempt.setRequestedResource(request.getRequestURI());
        attempt.setClientInfo(ClientInfo.fromRequest(request));
      };
      return this;
    }

    public Builder fromUser(SecurityUser user) {
      if (user != null) {
        attempt.setUserId(user.getId());
        attempt.setUsername(user.getUsername());
      }
      return this;
    }

  }

}
