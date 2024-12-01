package com.demo.security.spring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.AuthorityGroups;
import com.demo.security.spring.utils.AuthorityUserPrivileges;
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

    @Test
    void testNotAuthorizedExternalUser() throws Exception {
        final String username = testDataGenerator.randomUsername();
        final String password = testDataGenerator.randomPassword();
        final SecurityUser user = testDataGenerator.generateExternalUser(username, password, true, u -> {
            u.getGroups().removeIf(it -> AuthorityGroups.GROUP_ACCOUNT_HOLDER.equals(it.getCode()));
        });
        mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isForbidden());

        userAuthorityManager.addAuthorities(user, List.of(
            AuthorityUserPrivileges.AUTH_SELF_LOAN_VIEW,
            AuthorityUserPrivileges.AUTH_SELF_CARD_EDIT,
            AuthorityUserPrivileges.AUTH_SELF_TRANSACTION_EDIT
        ));
        mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isForbidden());

        userAuthorityManager.addAuthority(user, AuthorityUserPrivileges.AUTH_SELF_CARD_VIEW);
        mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk());
    }

    @Test
    void testNotAuthorizedAdminUser() throws Exception {
        final String username = testDataGenerator.randomUsername();
        final String password = testDataGenerator.randomPassword();
        final SecurityUser user = testDataGenerator.generateAdminUser(username, password, true, u -> {
            u.getGroups().removeIf(it -> AuthorityGroups.GROUP_ADMIN_SYSTEM.equals(it.getCode()));
        });
        mockMvc.perform(get(AccountController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isForbidden());
        userAuthorityManager.addAuthorities(user, List.of(
            AuthorityUserPrivileges.AUTH_SELF_TRANSACTION_CREATE,
            AuthorityUserPrivileges.AUTH_SELF_CARD_APPLY,
            AuthorityUserPrivileges.AUTH_SELF_ACCOUNT_APPLY
        ));
        mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isForbidden());

        userAuthorityManager.addAuthority(user, AuthorityUserPrivileges.AUTH_SELF_CARD_VIEW);
        mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk());
    }

    @Test
    void testAuthorizedAdminUser() throws Exception {
        final String username = testDataGenerator.randomUsername();
        final String password = testDataGenerator.randomPassword();
        final SecurityUser user = testDataGenerator.generateAdminUser(username, password, true);
        mockMvc.perform(get(CardsController.RESOURCE_PATH)
                .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk());
    }

    private List<Card> asCards(String responseBody) throws JsonProcessingException {
        return Arrays.stream(objectMapper.readValue(responseBody, Card[].class)).toList();
    }
}