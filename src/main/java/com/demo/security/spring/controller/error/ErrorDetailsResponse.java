package com.demo.security.spring.controller.error;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class ErrorDetailsResponse {

  private String fieldName;

  private String errorCode;

  private String errorMessage;

  private Object rejectedValue;

}
