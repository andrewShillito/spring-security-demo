package com.demo.security.spring.service;

import com.demo.security.spring.model.AccountTransaction;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface BalanceService {

  List<AccountTransaction> getAllForUser(Authentication authentication);

  List<AccountTransaction> gettAllForUser();
}
