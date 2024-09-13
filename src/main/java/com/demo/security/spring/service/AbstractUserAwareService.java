package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public abstract class AbstractUserAwareService implements UserAware {

  @Getter
  @Setter
  private SecurityUserService securityUserService;

  public <T> T executeForUser(Authentication authentication, Function<SecurityUser, T> function) {
    if (!authentication.isAuthenticated()) {
      throw new InsufficientAuthenticationException("User is not authenticated " + authentication);
    }
    SecurityUser user = getAuthenticatedUser(authentication);
    if (user == null) {
      throw new UsernameNotFoundException(
          "User with authentication " + authentication + " not found despite being authenticated!");
    } else if (user.getId() == null) {
      throw new IllegalStateException("User id was null. This should not happen " + user);
    }
    return function.apply(user);
  }

  public <T> T executeForUser(Function<SecurityUser, T> function) {
    SecurityUser user = getAuthenticatedUser();
    if (user == null) {
      throw new UsernameNotFoundException("User is not authenticated!");
    } else if (user.getId() == null) {
      throw new IllegalStateException("User id was null. This should not happen " + user);
    }
    return function.apply(user);
  }
}
