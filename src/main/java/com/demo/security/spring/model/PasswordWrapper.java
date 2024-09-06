package com.demo.security.spring.model;

import com.demo.security.spring.validation.IsValidPassword;
import lombok.Getter;

/**
 * Used for validating a password value for a user prior to it being hashed
 */
@Getter
public class PasswordWrapper {

  @IsValidPassword
  private String password;

  public PasswordWrapper setPassword(String password) {
    this.password = password;
    return this;
  }
}
