package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.model.Loan;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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

    private static final String PARAM_USER_ID = "userId";

    @Test
    @WithMockUser
    void testInvalidUserIdParam() throws Exception {
        MvcResult result = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH))
            .andExpect(status().isBadRequest())
            .andReturn();
        DemoAssertions.assertBlank(result.getResponse().getContentAsString());
        List.of("", " ").stream().forEach(userIdParamValue -> {
            try {
                MvcResult tempResult = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
                        .param(PARAM_USER_ID, userIdParamValue))
                    .andExpect(status().isBadRequest())
                    .andReturn();
                DemoAssertions.assertBlank(tempResult.getResponse().getContentAsString());
            } catch (Exception e) {
                fail("Encountered exception when testing missing user id param " + userIdParamValue, e);
            }
        });
    }

    @Test
    void getLoanDetailsUnauthorized() throws Exception {
        testSecuredBaseUrlAuth(mockMvc, LoansController.LOANS_RESOURCE_PATH);
    }

    @Test
    void testCors() {
        _testCors(mockMvc, LoansController.LOANS_RESOURCE_PATH, "userId", "1", true);
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

        // TODO: add non-empty response population and testing
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