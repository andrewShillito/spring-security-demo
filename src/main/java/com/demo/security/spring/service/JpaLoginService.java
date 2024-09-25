package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.provisioning.UserDetailsManager;

@Builder
@Log4j2
public class JpaLoginService implements LoginService {

  private UserDetailsManager userDetailsManager;

  @Override
  public SecurityUser createUser(SecurityUser user) {
    userDetailsManager.createUser(user);
    return user;
  }

  @Override
  public SecurityUser getUser(Authentication authentication) {
    if (authentication != null && StringUtils.isNotBlank(authentication.getName())) {
      return (SecurityUser) userDetailsManager.loadUserByUsername(authentication.getName());
    }
    return null;
  }
}
