package com.demo.security.spring.repository;

import com.demo.security.spring.model.AccountTransaction;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface AccountTransactionRepository extends CrudRepository<AccountTransaction, Long> {

  List<AccountTransaction> findAllByUserIdOrderByTransactionDateDesc(Long userId);

}
