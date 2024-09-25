package com.demo.security.spring.controller.error;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@EqualsAndHashCode
@ToString
public class ValidationErrorDetailsResponse {

  private String fieldName;

  private String errorCode;

  private String errorMessage;

  private Object rejectedValue;

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ValidationErrorDetailsResponse errorDetailsResponse = new ValidationErrorDetailsResponse();

    public Builder clear() {
      errorDetailsResponse = new ValidationErrorDetailsResponse();
      return this;
    }

    public ValidationErrorDetailsResponse build() {
      return errorDetailsResponse;
    }

    public Builder fieldName(String to) {
      errorDetailsResponse.setFieldName(to);
      return this;
    }

    public Builder errorCode(String to) {
      errorDetailsResponse.setErrorCode(to);
      return this;
    }

    public Builder errorMessage(String to) {
      errorDetailsResponse.setErrorMessage(to);
      return this;
    }

    public Builder rejectedValue(Object to) {
      errorDetailsResponse.setRejectedValue(to);
      return this;
    }
  }

}
