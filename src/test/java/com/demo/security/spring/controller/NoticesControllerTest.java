package com.demo.security.spring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NoticesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getNotices() throws Exception {
        mockMvc.perform(get(NoticesController.NOTICES_RESOURCE_PATH)).andExpect(status().isOk()); // public access
        mockMvc.perform(get(NoticesController.NOTICES_RESOURCE_PATH).with(user("test-notices-controller"))).andExpect(status().isOk());
    }
}