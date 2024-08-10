package com.demo.security.spring.controller.error;

import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AuthenticationErrorDetailsResponse {

  private ZonedDateTime time;

  private int errorCode;

  private String errorMessage;

  private String requestUri;

  private String realm;

  private String additionalInfo;

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private AuthenticationErrorDetailsResponse authErrorResponse;

    public Builder() {
      this.authErrorResponse = new AuthenticationErrorDetailsResponse();
    }

    public Builder clear() {
      this.authErrorResponse = new AuthenticationErrorDetailsResponse();
      return this;
    }

    public Builder time(ZonedDateTime time) {
      authErrorResponse.setTime(time);
      return this;
    }

    public Builder errorCode(int errorCode) {
      authErrorResponse.setErrorCode(errorCode);
      return this;
    }

    public Builder errorMessage(String errorMessage) {
      authErrorResponse.setErrorMessage(errorMessage);
      return this;
    }

    public Builder requestUri(String requestUri) {
      authErrorResponse.setRequestUri(requestUri);
      return this;
    }

    public Builder realm(String realm) {
      authErrorResponse.setRealm(realm);
      return this;
    }

    public Builder additionalInfo(String additionalInfo) {
      authErrorResponse.setAdditionalInfo(additionalInfo);
      return this;
    }

    public AuthenticationErrorDetailsResponse build() {
      return authErrorResponse;
    }
  }
}
