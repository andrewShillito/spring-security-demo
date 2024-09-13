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

  @Override
  public Account findOneForUser(Authentication authentication) {
    // note that the original course project expects only a single account so I adhered to that here for now
    Function<SecurityUser, Account> function = u -> accountRepository.findByUserId(u.getId());
    return executeForUser(authentication, function);
  }
}
