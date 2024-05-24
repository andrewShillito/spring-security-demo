package com.demo.security.spring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getContactPage() throws Exception {
        mockMvc.perform(get(NoticesController.NOTICES_RESOURCE_PATH)).andExpect(status().isOk()); // public access
        mockMvc.perform(get(NoticesController.NOTICES_RESOURCE_PATH).with(user("test-contact-controller"))).andExpect(status().isOk());
    }
}