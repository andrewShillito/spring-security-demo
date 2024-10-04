package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.controller.error.ValidationErrorDetailsResponse;
import com.demo.security.spring.generate.ContactMessageGenerator;
import com.demo.security.spring.model.ContactMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Log4j2
@Transactional
class ContactControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContactMessageGenerator contactMessageGenerator;

    @Test
    void getContactPage() throws Exception {
        mockMvc.perform(get(ContactController.RESOURCE_PATH)).andExpect(status().isMethodNotAllowed());
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
        final MvcResult result = executeValidPost(original)
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
        ContactMessage actual = asMessage(result.getResponse().getContentAsString());
        assertEqualsExceptId(original, result.getResponse().getContentAsString());
        assertNotNull(actual.getContactId());
    }

    private ResultActions executeValidPost(String content) throws Exception {
        return mockMvc.perform(post(ContactController.RESOURCE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(content));
    }

    private MvcResult expectBadRequest(ResultActions resultActions) throws Exception {
        return resultActions.andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
    }

    @Test
    void testFieldValidationRules() {
        final List<String> testValues = new ArrayList<>();
        testValues.add(null);
        testValues.add("");
        testValues.add(" ");
        Arrays.stream(ContactMessage.class.getDeclaredFields())
            .filter(field -> Objects.equals(String.class, field.getType()))
            .forEach(field -> {
                testValues.forEach(value -> {
                    log.info(() -> "Testing field " + field.getName() + " validation with value " + (value == null ? "'null'" : "'" + value + "'"));
                    ContactMessage message = randomContactMessage();
                    ReflectionTestUtils.invokeSetterMethod(message, field.getName(), value);
                    try {
                        final MvcResult result = expectBadRequest(executeValidPost(asString(message)));
                        List<ValidationErrorDetailsResponse> responseContent = asErrorDetailsList(result, 1);
                        testErrorDetailsResponse(field.getName(), "".equals(value) ? null : value, responseContent.getFirst());
                    } catch (Exception e) {
                        fail(e);
                    }
                });
            });
    }

    private void testErrorDetailsResponse(String fieldName, String rejectedValue, ValidationErrorDetailsResponse actual) {
        final ValidationErrorDetailsResponse expectedErrorDetails = ValidationErrorDetailsResponse.builder()
            .fieldName(fieldName)
            .errorCode("NotBlank")
            .errorMessage("must not be blank")
            .rejectedValue(rejectedValue)
            .build();
        testErrorDetailsResponse(expectedErrorDetails, actual);
    }

    private void testErrorDetailsResponse(ValidationErrorDetailsResponse expected, ValidationErrorDetailsResponse actual) {
        assertEquals(expected, actual);
    }

    private List<ValidationErrorDetailsResponse> asErrorDetailsList(MvcResult mvcResult)
        throws IOException {
        List<Map> actualList = asList(mvcResult.getResponse().getContentAsString());
        return actualList.stream().map(this::asErrorDetails).toList();
    }

    private List<ValidationErrorDetailsResponse> asErrorDetailsList(MvcResult mvcResult, int expectedSize)
        throws IOException {
        final List<ValidationErrorDetailsResponse> result = asErrorDetailsList(mvcResult);
        assertEquals(result.size(), expectedSize);
        return result;
    }

    private ContactMessage randomContactMessage() {
        return contactMessageGenerator.generate(1).getFirst();
    }

    private String randomContactMessageString() throws IOException {
        return asString(randomContactMessage());
    }

    private void assertEqualsExceptId(String expected, String actual) throws Exception {
        final ContactMessage expectedMessage = asMessage(expected);
        final ContactMessage actualMessage = asMessage(actual);
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

    private ContactMessage asMessage(String contactMessageString) throws IOException {
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

    private ValidationErrorDetailsResponse asErrorDetails(Map source) {
        return ValidationErrorDetailsResponse.builder()
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