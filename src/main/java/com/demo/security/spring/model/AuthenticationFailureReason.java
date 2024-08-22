package com.demo.security.spring.model;

/**
 * The reason that an authentication attempt failed
 */
public enum AuthenticationFailureReason {

  ACCOUNT_EXPIRED,

  BAD_CREDENTIALS,

  CREDENTIALS_EXPIRED,

  DISABLED,

  LOCKED,

  NO_AUTHORITIES,

  USER_NOT_FOUND

}
