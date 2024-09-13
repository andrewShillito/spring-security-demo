package com.demo.security.spring.controller;

import com.demo.security.spring.model.AccountTransaction;
import com.demo.security.spring.service.BalanceService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

  public static final String RESOURCE_PATH = "/myBalance";

  private BalanceService balanceService;

  @Autowired
  public void setBalanceService(BalanceService balanceService) {
    this.balanceService = balanceService;
  }

  @GetMapping(RESOURCE_PATH)
  public List<AccountTransaction> getBalanceDetails(Authentication authentication) {
    return balanceService.getAllForUser(authentication);
  }
}
