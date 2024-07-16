package com.demo.security.spring.controller.error;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@EqualsAndHashCode
@ToString
public class ErrorDetailsResponse {

  private String fieldName;

  private String errorCode;

  private String errorMessage;

  private Object rejectedValue;

}
