package com.demo.security.spring.utils;

import com.demo.security.spring.model.Card;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CardGenerator extends AbstractGenerator<List<Card>> {

  @Override
  public List<Card> generate() {
    final List<Card> cards = new ArrayList<>();
    for (int i = 0; i < getItemCount(); i++) {
      cards.add(generateCard());
    }
    log.info(() -> "Generated " + cards.size() + " cards");
    return cards;
  }

  private Card generateCard() {
    final Card card = new Card();
    card.setCardNumber(faker.finance().creditCard());
    card.setCreatedDate(randomEntityCreatedDate());
    card.setTotalLimit(BigDecimal.valueOf(faker.random().nextDouble(1000, 10000)));
    card.setAmountUsed(BigDecimal.valueOf(faker.random().nextDouble(1000, card.getTotalLimit().doubleValue())));
    card.setAvailableAmount(card.getTotalLimit().subtract(card.getAmountUsed()));
    card.setCardType("Visa");
    return card;
  }
}
