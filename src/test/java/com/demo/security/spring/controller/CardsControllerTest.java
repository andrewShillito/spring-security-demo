package com.demo.security.spring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.SecurityUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
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
class CardsControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUnauthenticated() throws Exception {
        _testSecuredBaseUrlAuth(mockMvc, CardsController.RESOURCE_PATH);
    }

    @Test
    void testCors() throws Exception {
        _testCors(mockMvc, CardsController.RESOURCE_PATH, true);
    }

    @Test
    void testGetCards() throws Exception {
        final SecurityUser user = testDataGenerator.generateExternalUser(true);
        final SecurityUser otherUser = testDataGenerator.generateExternalUser(true);
        assertEquals("[]", mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        assertEquals("[]", mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(otherUser)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());

        List<Card> expectedCards = testDataGenerator.generateCards(user, 5);
        List<Card> actualCards = asCards(mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
        assertEquals(expectedCards.size(), actualCards.size());
        assertIterableEquals(expectedCards, actualCards);

        assertEquals("[]", mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(otherUser)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString());
    }

    private List<Card> asCards(String responseBody) throws JsonProcessingException {
        return Arrays.stream(objectMapper.readValue(responseBody, Card[].class)).toList();
    }
}