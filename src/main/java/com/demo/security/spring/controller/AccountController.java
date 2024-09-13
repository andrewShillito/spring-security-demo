package com.demo.security.spring.controller;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

  public static final String RESOURCE_PATH = "/myAccount";

  private AccountService accountService;

  @Autowired
  public void setAccountService(AccountService accountService) {
    this.accountService = accountService;
  }

  @GetMapping(RESOURCE_PATH)
  public Account getAccountDetails(Authentication authentication) {
    return accountService.findOneForUser(authentication);
  }
}
