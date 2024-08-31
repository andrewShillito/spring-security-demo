package com.demo.security.spring.controller;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.AccountRepository;
import com.demo.security.spring.service.UserDetailsManagerImpl;
import javax.naming.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

  public static final String RESOURCE_PATH = "/myAccount";

  private AccountRepository accountRepository;

  private UserDetailsManagerImpl userDetailsManager;

  @Autowired
  public void setAccountRepository(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @Autowired
  public void setUserDetailsManager(
      UserDetailsManagerImpl userDetailsManager) {
    this.userDetailsManager = userDetailsManager;
  }

  @GetMapping(RESOURCE_PATH)
  public Account getAccountDetails() throws AuthenticationException {
    // note that the original course project expects only a single account so I adhered to that here for now
    SecurityUser user = userDetailsManager.getAuthenticatedUser();
    if (user != null && user.getId() != null) {
      return accountRepository.findByUserId(user.getId());
    }
    throw new AuthenticationException("User is not authenticated");
  }
}
