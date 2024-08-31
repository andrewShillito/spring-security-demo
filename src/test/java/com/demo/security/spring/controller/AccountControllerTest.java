package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.SecurityUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAccountDetailsNotLoggedIn() throws Exception {
        mockMvc.perform(get(AccountController.RESOURCE_PATH)).andExpect(status().isUnauthorized());
        testSecuredBaseUrlAuth(mockMvc, AccountController.RESOURCE_PATH);
    }

    @Test
    void testCors() {
        _testCors(mockMvc, AccountController.RESOURCE_PATH, true);
    }

    @Test
    void testGetAccount() throws Exception {
        final SecurityUser user = testDataGenerator.generateExternalUser(true);
        final SecurityUser otherUser = testDataGenerator.generateExternalUser(true);
        assertEquals("", mockMvc.perform(get(AccountController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        assertEquals("", mockMvc.perform(get(AccountController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(otherUser)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        Account account = testDataGenerator.generateAccount(user);
        String accountResponseBody = mockMvc.perform(get(AccountController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        DemoAssertions.assertNotEmpty(accountResponseBody);
        Account actualAccount = asAccount(accountResponseBody);
        assertEquals(account, actualAccount);
        assertIterableEquals(account.getAccountTransactions(), actualAccount.getAccountTransactions());
        assertEquals("", mockMvc.perform(get(AccountController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(otherUser)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
    }

    private Account asAccount(String responseContent) throws JsonProcessingException {
        return objectMapper.readValue(responseContent, Account.class);
    }
}