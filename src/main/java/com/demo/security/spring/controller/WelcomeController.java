package com.demo.security.spring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    public static final String WELCOME_RESOURCE_PATH = "/welcome";

    @GetMapping(WELCOME_RESOURCE_PATH)
    public String welcome() {
        return "Welcome to basic auth demo";
    }
}
