package com.demo.security.spring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CardsController {

  public static final String CARDS_RESOURCE_PATH = "/myCards";

  @GetMapping(CARDS_RESOURCE_PATH)
  public String getCardDetails() {
    return "Placeholder card details";
  }
}
