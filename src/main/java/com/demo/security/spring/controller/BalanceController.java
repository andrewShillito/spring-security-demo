package com.demo.security.spring.controller;

import com.demo.security.spring.model.AccountTransaction;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.AccountTransactionRepository;
import com.demo.security.spring.service.UserDetailsManagerImpl;
import java.util.List;
import javax.naming.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

  public static final String RESOURCE_PATH = "/myBalance";

  private AccountTransactionRepository accountTransactionRepository;

  private UserDetailsManagerImpl userDetailsManager;

  @Autowired
  public void setAccountTransactionRepository(
      AccountTransactionRepository accountTransactionRepository) {
    this.accountTransactionRepository = accountTransactionRepository;
  }

  @Autowired
  public void setUserDetailsManager(
      UserDetailsManagerImpl userDetailsManager) {
    this.userDetailsManager = userDetailsManager;
  }

  @GetMapping(RESOURCE_PATH)
  public List<AccountTransaction> getBalanceDetails() throws AuthenticationException {
    SecurityUser user = userDetailsManager.getAuthenticatedUser();
    if (user != null && user.getId() != null) {
      return accountTransactionRepository.findAllByUserIdOrderByTransactionDateDesc(user.getId());
    }
    throw new AuthenticationException("User is not authenticated");
  }
}
