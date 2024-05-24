package com.demo.security.spring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoansControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void getLoansDetails() throws Exception {
        mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH).with(user("test-loans-controller"))).andExpect(status().isOk());
    }
}