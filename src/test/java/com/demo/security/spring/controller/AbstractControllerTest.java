package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.controller.error.AuthErrorDetailsResponse;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.service.UserAuthorityManager;
import com.demo.security.spring.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractControllerTest {

    protected final Faker faker = new Faker();

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected Environment environment;

    @Autowired
    protected TestDataGenerator testDataGenerator;

    @Autowired
    protected UserAuthorityManager userAuthorityManager;

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
    public void _testSecuredBaseUrlAuth(MockMvc mockMvc, String baseUrl) throws Exception {
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
        var authErrorBody = objectMapper.readValue(body, AuthErrorDetailsResponse.class);
        assertNotNull(authErrorBody);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), authErrorBody.getErrorCode());
        assertEquals(expectedMessage, authErrorBody.getErrorMessage());
        assertEquals(expectedUri, authErrorBody.getRequestUri());
        assertEquals("http://localhost:80", authErrorBody.getRealm());
        assertEquals("Example additional info", authErrorBody.getAdditionalInfo());
        DemoAssertions.assertDateIsNowIsh(authErrorBody.getTime());
        assertEquals(ZoneId.of("UTC"), authErrorBody.getTime().getZone());
    }

    public void _testCors(MockMvc mockMvc, String resourcePath, boolean requiresUser) {
        _testCors(mockMvc, resourcePath, null, null, requiresUser);
    }

    public void _testCors(MockMvc mockMvc, String resourcePath, String paramName, String paramValue, boolean requiresUser) {
        final String exampleInvalidUrl = "http://www.someOtherSite.com";
        final SecurityUser user = testDataGenerator.generateExternalUser(true);
        List.of(exampleInvalidUrl, faker.internet().url()).forEach(invalidOrigin -> {
            try {
                MvcResult invalidResult = mockMvc.perform(get(resourcePath)
                        .with(user(user))
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
                    if (requiresUser) {
                        mockMvc.perform(get(resourcePath).with(user(user))
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
                                .with(user(user)))
                            .andExpect(status().isOk());
                    } else {
                        mockMvc.perform(get(resourcePath).header("Origin", origin)).andExpect(status().isOk());
                    }
                }
            } catch (Exception e) {
                fail("Failed to access resource " + resourcePath + " from origin " + origin + " with params " + paramName + ": " + paramValue, e);
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

    /**
     * Return the schema returned to logged in users in {@link Constants#SWAGGER_SCHEMA_URL}
     * so it can be validated
     * @param mockMvc the mockMvc object to use to make the request
     * @param securityUser a valid user who can log in
     * @return Map open-api generated json schema
     * @throws Exception ClassCastException could be thrown as unchecked cast occurs
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSwaggerSchema(MockMvc mockMvc, SecurityUser securityUser)
        throws Exception {
        var response = mockMvc.perform(get(Constants.SWAGGER_SCHEMA_URL).with(user(securityUser)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()
            .getResponse();
        DemoAssertions.assertNotEmpty(response.getContentAsString());
        return (Map<String, Object>) objectMapper.readValue(response.getContentAsString(), Map.class);
    }
}
