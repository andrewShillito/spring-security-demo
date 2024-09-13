package com.demo.security.spring.service;

import com.demo.security.spring.model.AccountTransaction;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.AccountTransactionRepository;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;

@Log4j2
@Builder
public class BalanceServiceImpl extends AbstractUserAwareService implements BalanceService {

  private AccountTransactionRepository accountTransactionRepository;

  @Override
  public List<AccountTransaction> getAllForUser(Authentication authentication) {
    final Function<SecurityUser, List<AccountTransaction>> getAllFunction = u -> accountTransactionRepository
        .findAllByUserIdOrderByTransactionDateDesc(u.getId());
    return executeForUser(authentication, getAllFunction);
  }

}
