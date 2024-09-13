package com.demo.security.spring.service;

import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.CardRepository;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;

@Log4j2
@Builder
public class CardServiceImpl extends AbstractUserAwareService implements CardService {

  private CardRepository cardRepository;

  private final Function<SecurityUser, List<Card>> getAllForUser = u -> cardRepository.findAllByUserId(u.getId());

  @Override
  public List<Card> getAllForUser(Authentication authentication) {
    return executeForUser(authentication, getAllForUser);
  }

  @Override
  public List<Card> getAllForUser() {
    return executeForUser(getAllForUser);
  }
}
