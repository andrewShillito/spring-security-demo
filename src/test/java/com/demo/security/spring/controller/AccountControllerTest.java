package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.api.ApiSchemaValidator;
import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.SecurityUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.MutablePair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
        _testSecuredBaseUrlAuth(mockMvc, AccountController.RESOURCE_PATH);
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

    @Test
    void testAccountApiSchema() throws Exception {
        final SecurityUser user = testDataGenerator.generateExternalUser(true);
        Map<String, Object> schema = getSwaggerSchema(mockMvc, user);

        // this is PoC for api schema validation - can be improved
        var apiSchemaValidator = new ApiSchemaValidator(schema);
        assertNotNull(apiSchemaValidator);
        apiSchemaValidator.hasPath(AccountController.RESOURCE_PATH);
        apiSchemaValidator.hasMethod(AccountController.RESOURCE_PATH, HttpMethod.GET);
        apiSchemaValidator.hasNotGotPath(faker.internet().url());
        apiSchemaValidator.hasNotGotMethod(AccountController.RESOURCE_PATH, HttpMethod.POST);
        apiSchemaValidator.hasMethodCount(AccountController.RESOURCE_PATH, 1);
        apiSchemaValidator.validate(
            AccountController.RESOURCE_PATH,
            HttpMethod.GET,
            "getAccountDetails",
            List.of("account", "get", "v1"),
            false,
            false,
            null,
            200,
            "OK",
            List.of(new MutablePair<>(MediaType.APPLICATION_JSON_VALUE, "Account"))
        );
    }

    private Account asAccount(String responseContent) throws JsonProcessingException {
        return objectMapper.readValue(responseContent, Account.class);
    }
}