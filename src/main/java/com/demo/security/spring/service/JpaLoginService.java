package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
@Log4j2
public class JpaLoginService implements LoginService {

  private SecurityUserRepository securityUserRepository;

  private PasswordEncoder passwordEncoder;

  @Override
  public SecurityUser createUser(SecurityUser user) {
    // the DB has unique index for usernames so will validate at insert - however this validation is case-sensitive
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return securityUserRepository.save(user);
  }

  @Override
  public SecurityUser getUser(Authentication authentication) {
    if (authentication != null && StringUtils.isNotBlank(authentication.getName())) {
      return securityUserRepository.getSecurityUserByUsername(authentication.getName());
    }
    return null;
  }
}
