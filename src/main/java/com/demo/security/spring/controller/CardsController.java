package com.demo.security.spring.controller;

import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.CardRepository;
import com.demo.security.spring.service.CardService;
import com.demo.security.spring.service.SecurityUserService;
import java.util.List;
import javax.naming.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CardsController {

  public static final String RESOURCE_PATH = "/myCards";

  private CardService cardService;

  @Autowired
  public void setCardService(CardService cardService) {
    this.cardService = cardService;
  }

  @GetMapping(RESOURCE_PATH)
  public List<Card> getCardDetails(Authentication authentication) throws AuthenticationException {
    return cardService.getAllForUser(authentication);
  }
}
