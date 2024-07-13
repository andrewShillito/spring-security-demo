package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.generate.ContactMessagesFileGenerator;
import com.demo.security.spring.model.ContactMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContactMessagesFileGenerator contactMessagesFileGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getContactPage() throws Exception {
        mockMvc.perform(get(ContactController.CONTACT_RESOURCE_PATH)).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void createContactMessageNotLoggedIn() throws Exception {
        testSuccessfulRequest();
    }

    @Test
    @WithMockUser
    void createContactMessageAsUser() throws Exception {
        testSuccessfulRequest();
    }

    private void testSuccessfulRequest() throws Exception {
        final String original = randomContactMessageString();
        final MvcResult result = mockMvc.perform(post(ContactController.CONTACT_RESOURCE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(original))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
        ContactMessage actual = fromString(result.getResponse().getContentAsString());
        assertEqualsExceptId(original, result.getResponse().getContentAsString());
        assertNotNull(actual.getContactId());
    }

    private ContactMessage randomContactMessage() {
        return contactMessagesFileGenerator.generate(1).getFirst();
    }

    private String randomContactMessageString() throws IOException {
        return asString(randomContactMessage());
    }

    private void assertEqualsExceptId(String expected, String actual) throws Exception {
        final ContactMessage expectedMessage = fromString(expected);
        final ContactMessage actualMessage = fromString(actual);
        assertEqualsExceptId(expectedMessage, actualMessage);
    }

    private void assertEqualsExceptId(ContactMessage expected, ContactMessage actual) {
        assertEquals(expected.getContactEmail(), actual.getContactEmail());
        assertEquals(expected.getContactName(), actual.getContactName());
        assertEquals(expected.getSubject(), actual.getSubject());
        assertNotNull(actual.getCreated());
        DemoAssertions.assertDateIsNowIsh(actual.getCreated());
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    private ContactMessage fromString(String contactMessageString) throws IOException {
        return objectMapper.readValue(contactMessageString, ContactMessage.class);
    }

    private String asString(ContactMessage contactMessage) throws IOException {
        return objectMapper.writeValueAsString(contactMessage);
    }
}