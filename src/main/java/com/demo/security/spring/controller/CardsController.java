package com.demo.security.spring.controller;

import com.demo.security.spring.model.Card;
import com.demo.security.spring.repository.CardRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CardsController {

  public static final String CARDS_RESOURCE_PATH = "/myCards";

  private CardRepository cardRepository;

  @Autowired
  public void setCardRepository(CardRepository cardRepository) {
    this.cardRepository = cardRepository;
  }

  @GetMapping(CARDS_RESOURCE_PATH)
  public List<Card> getCardDetails(@RequestParam long userId) {
    return cardRepository.findAllByUserId(userId);
  }
}
