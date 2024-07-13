package com.demo.security.spring.controller;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

  public static final String ACCOUNT_RESOURCE_PATH = "/myAccount";

  private AccountRepository accountRepository;

  @Autowired
  public void setAccountRepository(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  @GetMapping(ACCOUNT_RESOURCE_PATH)
  public Account getAccountDetails(@RequestParam long userId) {
    // note that the original course project expects only a single account so I adhered to that here for now
    return accountRepository.findByUserId(userId);
  }
}
