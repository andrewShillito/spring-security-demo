package com.demo.security.spring.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticesController {

    public static final String NOTICES_RESOURCE_PATH = "/notices";

    @GetMapping(NOTICES_RESOURCE_PATH)
    public String getNotices() {
        return "Placeholder for notices";
    }
}
