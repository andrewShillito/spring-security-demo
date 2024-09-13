package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import org.springframework.security.core.Authentication;

public interface UserAware {

  default SecurityUser getAuthenticatedUser() {
    return getSecurityUserService().getAuthenticatedUser();
  }

  default SecurityUser getAuthenticatedUser(Authentication authentication) {
    return getSecurityUserService().getAuthenticatedUser(authentication);
  }

  SecurityUserService getSecurityUserService();

}
