package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.controller.error.AuthenticationErrorDetailsResponse;
import com.demo.security.spring.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.util.List;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractControllerTest {

    protected final Faker faker = new Faker();

    private static final String EXTERNAL_USER_NAME = "user";
    private static final String EXTERNAL_USER_PASSWORD = "password";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected Environment environment;

    public String getTestUserName() {
        return this.getClass().getName().substring(0, 1).toLowerCase() + this.getClass().getName().substring(1);
    }

    public String getTestParam() {
        return "testParam=true";
    }

    /**
     * Common test steps for asserting that accessing a given endpoint returns unatuh
     * testing the security requirements of a resource for which the user must be logged
     * but for which there is no specific required role.
     * @param mockMvc
     * @param baseUrl
     * @throws Exception
     */
    public void testSecuredBaseUrlAuth(MockMvc mockMvc, String baseUrl) throws Exception {
        final String expectedErrorMessage = "Full authentication is required to access this resource";
        testUnauthorizedErrorResponseBody(
            executeMockUnauthorizedRequest(mockMvc, baseUrl),
            expectedErrorMessage,
            baseUrl);
        testUnauthorizedErrorResponseBody(
            executeMockUnauthorizedRequest(mockMvc, baseUrl + "/"),
            expectedErrorMessage,
            baseUrl + "/");
        testUnauthorizedErrorResponseBody(
            executeMockUnauthorizedRequest(mockMvc, baseUrl + "/?" + getTestParam()),
            expectedErrorMessage,
            baseUrl + "/");
        testUnauthorizedErrorResponseBody(
            executeMockUnauthorizedRequest(mockMvc, baseUrl + "/", "userId", "1"),
            expectedErrorMessage,
            baseUrl + "/");
    }

    private MockHttpServletResponse executeMockUnauthorizedRequest(MockMvc mockMvc, String url) throws Exception {
        return mockMvc.perform(get(url)).andExpect(status().isUnauthorized()).andReturn().getResponse();
    }

    private MockHttpServletResponse executeMockUnauthorizedRequest(MockMvc mockMvc, String url, String paramName, String paramValue) throws Exception {
        return mockMvc.perform(get(url).param(paramName, paramValue)).andExpect(status().isUnauthorized()).andReturn().getResponse();
    }

    protected void testUnauthorizedErrorResponseBody(
        final MockHttpServletResponse response,
        final String expectedMessage,
        final String expectedUri
        ) throws Exception {
        // testing of expected error message body
        final String body = response.getContentAsString();
        assertNotNull(body);
        DemoAssertions.assertNotEmpty(body);
        var authErrorBody = objectMapper.readValue(body, AuthenticationErrorDetailsResponse.class);
        assertNotNull(authErrorBody);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), authErrorBody.getErrorCode());
        assertEquals(expectedMessage, authErrorBody.getErrorMessage());
        assertEquals(expectedUri, authErrorBody.getRequestUri());
        assertEquals("http://localhost:80", authErrorBody.getRealm());
        assertEquals("Example additional info", authErrorBody.getAdditionalInfo());
        DemoAssertions.assertDateIsNowIsh(authErrorBody.getTime());
        assertEquals(ZoneId.of("UTC"), authErrorBody.getTime().getZone());
    }

    public void _testCors(MockMvc mockMvc, String resourcePath, String paramName, String paramValue, boolean requiresUser) {
        final String exampleInvalidUrl = "http://www.someOtherSite.com";
        List.of(exampleInvalidUrl, faker.internet().url()).forEach(invalidOrigin -> {
            try {
                MvcResult invalidResult = mockMvc.perform(get(resourcePath)
                        .with(user(EXTERNAL_USER_NAME).password(EXTERNAL_USER_PASSWORD))
                        .header("Origin", invalidOrigin))
                    .andExpect(status().isForbidden())
                    .andReturn();
                assertEquals("Invalid CORS request", invalidResult.getResponse().getContentAsString());
                // invalid options request
                invalidResult = mockMvc.perform(options(resourcePath)
                    .header("Access-Control-Request-Method", "GET")
                    .header("Origin", invalidOrigin)
                ).andReturn();
                assertEquals(403, invalidResult.getResponse().getStatus());
                assertEquals("Invalid CORS request", invalidResult.getResponse().getContentAsString());
            } catch (Exception e) {
                fail("Failed to test cors for resource " + resourcePath + " with unexpected error", e);
            }
        });

        // allowed cors requests
        for (String origin : Constants.EXAMPLE_ALLOWED_CORS_PATHS) {
            try {
                if (paramName != null && paramValue != null) {
                    // TODO: for endpoints which are accessing user-specific data this will change in the future
                    //  to deny users from accessing any user info but their own
                    if (requiresUser) {
                        mockMvc.perform(get(resourcePath).with(user(EXTERNAL_USER_NAME).password(EXTERNAL_USER_PASSWORD))
                                .param(paramName, paramValue)
                                .header("Origin", origin))
                            .andExpect(status().isOk());
                    } else {
                        mockMvc.perform(get(resourcePath)
                                .param(paramName, paramValue)
                                .header("Origin", origin))
                            .andExpect(status().isOk());
                    }
                } else {
                    if (requiresUser) {
                        mockMvc.perform(get(resourcePath).header("Origin", origin)
                                .with(user(EXTERNAL_USER_NAME).password(EXTERNAL_USER_PASSWORD)))
                            .andExpect(status().isOk());
                    } else {
                        mockMvc.perform(get(resourcePath).header("Origin", origin)).andExpect(status().isOk());
                    }
                }
            } catch (Exception e) {
                fail("Failed to access resource " + resourcePath + " from origin " + origin + " with params " + paramName + ": " + paramValue);
            }
        }
    }

    /**
     * Common test steps for testing the security requirements of a resource for which the user does NOT have to be logged in
     * @param mockMvc
     * @param baseUrl
     * @throws Exception
     */
    public void testUnSecuredBaseUrlAuth(MockMvc mockMvc, String baseUrl) throws Exception {
        final String testUserName = getTestUserName();
        mockMvc.perform(get(baseUrl)).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/")).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/" + "?" + getTestParam())).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl).with(user(testUserName))).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/").with(user(testUserName))).andExpect(status().isOk());
        mockMvc.perform(get(baseUrl + "/" + "?" + getTestParam()).with(user(testUserName))).andExpect(status().isOk());
    }
}
