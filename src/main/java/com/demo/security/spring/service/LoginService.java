package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;

public interface LoginService {

  SecurityUser createUser(SecurityUser user);

}
