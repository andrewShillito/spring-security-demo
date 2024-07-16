package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.controller.error.ErrorDetailsResponse;
import com.demo.security.spring.generate.ContactMessagesFileGenerator;
import com.demo.security.spring.model.ContactMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
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

    @Test
    void testCreateContactMessageMessageValidation() throws Exception {
        ContactMessage message = randomContactMessage();
        message.setMessage(null);
        final MvcResult result = mockMvc.perform(post(ContactController.CONTACT_RESOURCE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asString(message)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        List<Map> actualList = asList(result.getResponse().getContentAsString());
        assertNotNull(actualList);
        assertEquals(1, actualList.size());
        // test content of response
        final ErrorDetailsResponse expectedErrorDetails = ErrorDetailsResponse.builder()
            .fieldName("message")
            .errorCode("NotEmpty")
            .errorMessage("must not be empty")
            .rejectedValue(null)
            .build();
        final ErrorDetailsResponse actualErrorDetails = asErrorDetails(actualList.getFirst());
        assertEquals(expectedErrorDetails, actualErrorDetails);
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

    private Map<String, Object> asMap(String content) throws IOException {
        return objectMapper.readerForMapOf(Object.class).readValue(content);
    }

    private List<Map> asList(String content) throws IOException {
        return objectMapper.readerForListOf(Map.class).readValue(content);
    }

    private List<?> asListOfType(String content, Class<?> clazz) throws IOException {
        return objectMapper.readerForListOf(clazz).readValue(content);
    }

    private ErrorDetailsResponse asErrorDetails(Map source) {
        return ErrorDetailsResponse.builder()
            .fieldName(getStringErrorField(source, "fieldName"))
            .errorCode(getStringErrorField(source, "errorCode"))
            .errorMessage(getStringErrorField(source, "errorMessage"))
            .rejectedValue(source.get("rejectedValue"))
            .build();
    }

    private String getStringErrorField(Map source, String name) {
        if (source.get(name) instanceof String) {
            return (String) source.get(name);
        }
        fail("Expected string type for field name " + name + " in source map " + source);
        return null; // unreachable but required by compiler
    }


}