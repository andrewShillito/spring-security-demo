package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@Builder
@Log4j2
public class JpaLoginService implements LoginService {

  private SecurityUserRepository securityUserRepository;

  @Override
  public SecurityUser createUser(SecurityUser user) {
    // the DB has unique index for usernames so will validate at insert - however this validation is case-sensitive
    return securityUserRepository.save(user);
  }
}
