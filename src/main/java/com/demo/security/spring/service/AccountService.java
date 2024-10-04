package com.demo.security.spring.service;

import com.demo.security.spring.model.Account;
import org.springframework.security.core.Authentication;

public interface AccountService {

  Account findOneForUser(Authentication authentication);

  Account findOneForUser();

}
