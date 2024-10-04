package com.demo.security.spring.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface SecurityUserValidationService {

  void validateUser(UserDetails user, boolean validatePassword);

  void validateUserPassword(UserDetails user);

}
