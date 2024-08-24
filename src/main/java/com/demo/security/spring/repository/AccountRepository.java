package com.demo.security.spring.repository;

import com.demo.security.spring.model.Account;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, Long> {

  @Query(value = "SELECT a FROM Account a"
      + " LEFT JOIN AccountTransaction t ON a.accountNumber = t.account.accountNumber AND t.userId = ?1"
      + " WHERE a.userId = ?1 ORDER BY t.transactionDate DESC")
  List<Account> findAllByUserId(Long userId);

  @Query(value = "SELECT a FROM Account a"
      + " LEFT JOIN AccountTransaction t ON a.accountNumber = t.account.accountNumber AND t.userId = ?1"
      + " WHERE a.userId = ?1 ORDER BY t.transactionDate DESC LIMIT 1")
  Account findByUserId(Long userId);

}
