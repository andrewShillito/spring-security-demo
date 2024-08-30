package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.SecurityUser;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoansControllerTest extends AbstractControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void testInvalidParams() throws Exception {
        SecurityUser user = testDataGenerator.generateExternalUser(true);
        MvcResult result = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user))
                .param("userId", "1")) // doesn't look at this param anymore
            .andExpect(status().isOk())
            .andReturn();
        assertEquals("[]", result.getResponse().getContentAsString());

        Stream.of("", " ", faker.internet().domainWord()).forEach(paramValue -> {
            try {
                MvcResult tempResult = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
                        .with(SecurityMockMvcRequestPostProcessors.user(user))
                        .param(faker.internet().domainWord(), paramValue))
                    .andExpect(status().isOk())
                    .andReturn();
                // user doesn't have any loans
                assertEquals("[]", tempResult.getResponse().getContentAsString());
            } catch (Exception e) {
                fail("Encountered exception when testing missing user id param " + paramValue, e);
            }
        });
    }

    @Test
    void getLoanDetailsUnauthorized() throws Exception {
        testSecuredBaseUrlAuth(mockMvc, LoansController.LOANS_RESOURCE_PATH);
    }

    @Test
    void testCors() {
        _testCors(mockMvc, LoansController.LOANS_RESOURCE_PATH, true);
    }

    @Test
    @WithMockUser
    void getLoansDetails() throws Exception {
        final String username = testDataGenerator.randomUsername();
        final String userRawPassword = testDataGenerator.randomPassword();
        SecurityUser user = testDataGenerator.generateExternalUser(username, userRawPassword, true);
        List<Loan> expectedLoans = testDataGenerator.generateLoans(user, 5);

        final MvcResult result = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk())
            .andReturn();
        final List<Loan> actualLoans = asLoans(result.getResponse().getContentAsString());
        assertNotNull(actualLoans);
        assertEquals(expectedLoans.size(), actualLoans.size());

    }

    private List<Loan> asLoans(String loansJson) {
        try {
            Loan[] temp = objectMapper.readValue(loansJson, Loan[].class);
            return Arrays.stream(temp).toList();
        } catch (IOException e) {
            fail("Failed to deserialize Loans list", e);
        } catch (Exception e) {
            fail("Failed when creating loans list from response body", e);
        }
        return null;
    }
}