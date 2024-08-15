package com.demo.security.spring.controller.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(value = Include.NON_EMPTY)
public class AuthErrorDetailsResponse {

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

    private AuthErrorDetailsResponse authErrorResponse;

    public Builder() {
      this.authErrorResponse = new AuthErrorDetailsResponse();
    }

    public Builder clear() {
      this.authErrorResponse = new AuthErrorDetailsResponse();
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

    public AuthErrorDetailsResponse build() {
      return authErrorResponse;
    }
  }
}
