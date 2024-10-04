package com.demo.security.spring.service;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.AccountRepository;
import java.util.function.Function;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;

@Log4j2
@Builder
public class AccountServiceImpl extends AbstractUserAwareService implements AccountService {

  private AccountRepository accountRepository;

  private final Function<SecurityUser, Account> getOneForUser = u -> accountRepository.findByUserId(u.getId());

  @Override
  public Account findOneForUser(Authentication authentication) {
    // note that the original course project expects only a single account per user so I adhered to that here for now
    return executeForUser(authentication, getOneForUser);
  }

  @Override
  public Account findOneForUser() {
    return executeForUser(getOneForUser);
  }
}
