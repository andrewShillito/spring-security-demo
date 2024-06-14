package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Builder
@Log4j2
public class InMemoryLoginService implements LoginService {

  private UserDetailsService userDetailsService;

  @Override
  public SecurityUser createUser(SecurityUser user) {
    if (!(userDetailsService instanceof InMemoryUserDetailsManager inMemoryUserDetailsManager)) {
      throw new RuntimeException("Runtime userDetailsService has unexpected type " + userDetailsService.getClass().getName()
          + " TODO: implement UserDetailsManager instead of current approach.");
    }
    if (inMemoryUserDetailsManager.userExists(user.getUsername())) {
      throw new IllegalArgumentException("User with username " + user.getUsername() + " already exists!");
    }
    try {
      inMemoryUserDetailsManager.createUser(user);
    } catch (Exception e) {
      throw new RuntimeException("Encountered exception creating in-memory user " + user, e);
    }
    return user;
  }
}
