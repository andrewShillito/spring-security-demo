package com.demo.security.spring.controller;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

  @Operation( tags = { "account", "get", "v1" })
  @GetMapping(value = RESOURCE_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
  public Account getAccountDetails(Authentication authentication) {
    return accountService.findOneForUser(authentication);
  }
}
