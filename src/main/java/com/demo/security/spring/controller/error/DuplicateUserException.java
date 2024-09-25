package com.demo.security.spring.controller.error;

public class DuplicateUserException extends RuntimeException {

  public static final String ERROR_CODE = "DUPLICATE_USERNAME";

  public static final String ERROR_MESSAGE_TEMPLATE = "A user matching username %s already exists";

  private final String username;

  public DuplicateUserException(String username) {
    super(String.format(ERROR_MESSAGE_TEMPLATE, username));
    this.username = username;
  }

  public DuplicateUserException(String username, Throwable cause) {
    super(String.format(ERROR_MESSAGE_TEMPLATE, username));
    this.username = username;
  }
}
