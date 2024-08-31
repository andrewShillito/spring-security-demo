package com.demo.security.spring.repository;

import com.demo.security.spring.model.Loan;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface LoanRepository extends CrudRepository<Loan, Long> {

  @Query(value = "SELECT l FROM Loan l WHERE l.userId = ?1 ORDER BY l.startDate DESC")
  List<Loan> findAllByUserId(Long userId);

}
