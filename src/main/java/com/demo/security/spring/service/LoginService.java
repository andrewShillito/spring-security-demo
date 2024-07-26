package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import org.springframework.security.core.Authentication;

public interface LoginService {

  SecurityUser createUser(SecurityUser user);

  SecurityUser getUser(Authentication authentication);
}
