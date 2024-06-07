package com.demo.security.spring.model;

import lombok.ToString;

@ToString
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
