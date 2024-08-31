package com.demo.security.spring.controller;

import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.CardRepository;
import com.demo.security.spring.service.UserDetailsManagerImpl;
import java.util.List;
import javax.naming.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CardsController {

  public static final String RESOURCE_PATH = "/myCards";

  private CardRepository cardRepository;

  private UserDetailsManagerImpl userDetailsManager;

  @Autowired
  public void setCardRepository(CardRepository cardRepository) {
    this.cardRepository = cardRepository;
  }

  @Autowired
  public void setUserDetailsManager(
      UserDetailsManagerImpl userDetailsManager) {
    this.userDetailsManager = userDetailsManager;
  }

  @GetMapping(RESOURCE_PATH)
  public List<Card> getCardDetails() throws AuthenticationException {
    SecurityUser user = userDetailsManager.getAuthenticatedUser();
    if (user != null && user.getId() != null) {
      return cardRepository.findAllByUserId(user.getId());
    }
    throw new AuthenticationException("User is not authenticated");
  }
}
