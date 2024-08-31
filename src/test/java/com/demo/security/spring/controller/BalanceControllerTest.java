package com.demo.security.spring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.AccountTransaction;
import com.demo.security.spring.model.SecurityUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BalanceControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUnauthenticated() throws Exception {
        testSecuredBaseUrlAuth(mockMvc, BalanceController.RESOURCE_PATH);
    }

    @Test
    void testCors() throws Exception {
        _testCors(mockMvc, BalanceController.RESOURCE_PATH, true);
    }

    @Test
    void testGetBalanceDetails() throws Exception {
        final SecurityUser user = testDataGenerator.generateExternalUser(true);
        final SecurityUser otherUser = testDataGenerator.generateExternalUser(true);
        assertEquals("[]", mockMvc.perform(get(BalanceController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        assertEquals("[]", mockMvc.perform(get(BalanceController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(otherUser)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        Account account = testDataGenerator.generateAccount(user);
        sortAccountTransactions(account.getAccountTransactions()); // sort to match expected return
        normalizeDates(account.getAccountTransactions());
        List<AccountTransaction> actualTransactions = asAccountTransaction(mockMvc.perform(get(BalanceController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        normalizeDates(actualTransactions);
        assertIterableEquals(account.getAccountTransactions(), actualTransactions);
    }

    private List<AccountTransaction> asAccountTransaction(String responseBody) throws JsonProcessingException {
        return Arrays.stream(objectMapper.readValue(responseBody, AccountTransaction[].class)).toList();
    }

    private void sortAccountTransactions(List<AccountTransaction> transactions) {
        transactions.sort(Comparator.comparing(AccountTransaction::getTransactionDate).reversed());
    }

    private void normalizeDates(List<AccountTransaction> transactions) {
        transactions.forEach(t -> {
            t.setTransactionDate(normalizeDate(t.getTransactionDate()));
            t.getCreatedDate().setCreated(normalizeDate(t.getCreatedDate().getCreated()));
        });
    }

    private ZonedDateTime normalizeDate(ZonedDateTime date) {
        return date.withZoneSameInstant(ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
    }
}