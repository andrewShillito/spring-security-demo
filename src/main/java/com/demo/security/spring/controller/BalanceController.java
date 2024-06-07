package com.demo.security.spring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BalanceController {

  public static final String BALANCE_RESOURCE_PATH = "/myBalance";

  @GetMapping(BALANCE_RESOURCE_PATH)
  public String getBalanceDetails() {
    return "Placeholder for balance details";
  }
}
