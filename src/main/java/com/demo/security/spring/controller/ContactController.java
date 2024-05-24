package com.demo.security.spring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactController {

    public static final String CONTACT_RESOURCE_PATH = "/contact";

    @GetMapping(CONTACT_RESOURCE_PATH)
    public String getContactPage() {
        return "Placeholder contact details page";
    }
}
