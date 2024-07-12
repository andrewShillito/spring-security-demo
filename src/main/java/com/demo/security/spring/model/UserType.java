package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.ToString;

@ToString
@JsonInclude(Include.NON_EMPTY)
public enum UserType {

  /**
   * External users of the application
   */
  external,

  /**
   * internal users of the application
   */
  internal
}
