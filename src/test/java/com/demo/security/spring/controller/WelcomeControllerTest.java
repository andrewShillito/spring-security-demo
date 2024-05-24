package com.demo.security.spring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WelcomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testWelcomeUnauthorized() throws Exception {
        mockMvc.perform(get(WelcomeController.WELCOME_RESOURCE_PATH)).andExpect(status().isUnauthorized());
    }

    @Test
    void testWelcomeBasicAuth() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(WelcomeController.WELCOME_RESOURCE_PATH).with(user("welcome-user")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN + ";charset=UTF-8"))
                .andReturn();
        assertEquals("Welcome to basic auth demo", mvcResult.getResponse().getContentAsString());
    }

}