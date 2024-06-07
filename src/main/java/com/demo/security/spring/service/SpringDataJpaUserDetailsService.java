package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.google.common.base.Preconditions;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Builder
public class SpringDataJpaUserDetailsService implements UserDetailsService {

  private SecurityUserRepository securityUserRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Preconditions.checkArgument(StringUtils.isNotBlank(username),
        "Username " + username + " is not valid");
    final SecurityUser user = securityUserRepository.getSecurityUserByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("User with username " + username + " was not found.");
    }
    return user;
  }
}
