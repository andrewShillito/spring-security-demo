package com.demo.security.spring.generate;

import com.demo.security.spring.model.Card;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;

@Log4j2
public class CardGenerator extends AbstractGenerator<List<Card>> {

  public static final String DEFAULT_OUTPUT_FILE = "example-cards.json";

  public CardGenerator(Faker faker,
      ObjectMapper objectMapper) {
    this(faker, objectMapper, DEFAULT_ITEM_COUNT);
  }

  public CardGenerator(Faker faker, ObjectMapper objectMapper, int itemCount) {
    super(faker, objectMapper, itemCount);
  }

  @Override
  public List<Card> generate() {
    return generate(getItemCount());
  }

  @Override
  public List<Card> generate(int count) {
    final List<Card> cards = new ArrayList<>();
    for (int i = 0; i < count; i++) {
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
