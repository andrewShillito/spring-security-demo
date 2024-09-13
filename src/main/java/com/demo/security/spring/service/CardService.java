package com.demo.security.spring.service;

import com.demo.security.spring.model.Card;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface CardService {

  List<Card> getAllForUser(Authentication authentication);

}
