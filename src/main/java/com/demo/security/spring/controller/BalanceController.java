package com.demo.security.spring.controller;

import com.demo.security.spring.model.AccountTransaction;
import com.demo.security.spring.repository.AccountTransactionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

  public static final String BALANCE_RESOURCE_PATH = "/myBalance";

  private AccountTransactionRepository accountTransactionRepository;

  @Autowired
  public void setAccountTransactionRepository(
      AccountTransactionRepository accountTransactionRepository) {
    this.accountTransactionRepository = accountTransactionRepository;
  }

  @GetMapping(BALANCE_RESOURCE_PATH)
  public List<AccountTransaction> getBalanceDetails(@RequestParam long userId) {
    return accountTransactionRepository.findAllByUserIdOrderByTransactionDateDesc(userId);
  }
}
