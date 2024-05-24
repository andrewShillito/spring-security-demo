package com.demo.security.spring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    public static final String ACCOUNT_RESOURCE_PATH = "/myAccount";

    @GetMapping( ACCOUNT_RESOURCE_PATH)
    public String getAccountDetails() {
        return "Placeholder account details";
    }
}
