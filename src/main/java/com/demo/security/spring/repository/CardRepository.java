package com.demo.security.spring.repository;

import com.demo.security.spring.model.Card;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface CardRepository extends CrudRepository<Card, Long> {

  List<Card> findAllByUserIdOrderById(Long userId);

}
