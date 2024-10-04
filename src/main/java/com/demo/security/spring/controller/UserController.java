package com.demo.security.spring.controller;

import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserController.RESOURCE_PATH)
public class UserController {

  public static final String RESOURCE_PATH = "/user";

  private LoginService loginService;

  @Autowired
  public void setLoginService(LoginService loginService) {
    this.loginService = loginService;
  }

  @GetMapping
  public ResponseEntity<SecurityUser> getUserDetails(Authentication authentication) {
    if (authentication != null) {
      return ResponseEntity.status(HttpStatus.OK).body(loginService.getUser(authentication));
    }
    // the user should not be able to get to this endpoint without being logged in anyways
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
  }

}
