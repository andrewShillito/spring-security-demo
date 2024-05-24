package com.demo.security.spring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoansController {

    public static final String LOANS_RESOURCE_PATH = "/myLoans";

    @GetMapping(LOANS_RESOURCE_PATH)
    public String getLoansDetails() {
        return "Placeholder for loan details";
    }
}
