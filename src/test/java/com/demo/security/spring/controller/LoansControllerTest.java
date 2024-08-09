package com.demo.security.spring.controller;

import com.demo.security.spring.model.Loan;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoansControllerTest extends AbstractControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private static final String PARAM_USER_ID = "userId";

    @Test
    @WithMockUser
    void testInvalidUserIdParam() throws Exception {
        MvcResult result = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH))
            .andExpect(status().isBadRequest())
            .andReturn();
        assertBlank(result.getResponse().getContentAsString());
        List.of("", " ").stream().forEach(userIdParamValue -> {
            try {
                MvcResult tempResult = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
                        .param(PARAM_USER_ID, userIdParamValue))
                    .andExpect(status().isBadRequest())
                    .andReturn();
                assertBlank(tempResult.getResponse().getContentAsString());
            } catch (Exception e) {
                fail("Encountered exception when testing missing user id param " + userIdParamValue, e);
            }
        });
    }

    @Test
    void getLoanDetailsUnauthorized() throws Exception {
        mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH).param(PARAM_USER_ID, String.valueOf(1)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getLoansDetails() throws Exception {
        final MvcResult result = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH).param(PARAM_USER_ID, "1"))
            .andExpect(status().isOk())
            .andReturn();
        final List<Loan> loans = asLoans(result.getResponse().getContentAsString());
        assertNotNull(loans);
        assertEquals(0, loans.size());
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

    void assertLoansAreEmpty(List<Loan> loans) {
        assertTrue(loans == null || loans.isEmpty(), "Expected loans to be empty but found " + loans);
    }

    void assertBlank(String toTest) {
        assertTrue(StringUtils.isBlank(toTest), "Expected string to be blank but found " + toTest);
    }

    void assertEmpty(String toTest) {
        assertTrue(StringUtils.isEmpty(toTest), "Expected string to be empty but found " + toTest);
    }
}