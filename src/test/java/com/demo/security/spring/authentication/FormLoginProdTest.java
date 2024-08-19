package com.demo.security.spring.authentication;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.controller.LoansController;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.SpringProfileConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, properties = "server.port=8443")
@ActiveProfiles(value = { SpringProfileConstants.DEFAULT, SpringProfileConstants.PRODUCTION, SpringProfileConstants.H2 })
@AutoConfigureMockMvc
@Transactional
public class FormLoginProdTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected TestDataGenerator testDataGenerator;

  @Value("${server.port}")
  private Integer serverPort;

  @Test
  void testFormLoginIsAvailable() throws Exception {
    System.out.println("Server port " + serverPort);
    mockMvc.perform(get("/login"))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("https://localhost/login"));
  }

  @Test
  void testInvalidLogons() throws Exception {
    // non-existent user
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, testDataGenerator.randomUsername() + "invalid", "invalid", true);
    // invalid external user password
    final String username = testDataGenerator.randomUsername();
    final String internalUsername = username + "internal";
    final String userRawPassword = testDataGenerator.randomPassword();
    testDataGenerator.generateExternalUser(username, userRawPassword, true);
    testDataGenerator.generateInternalUser(internalUsername, userRawPassword, true);
    // correct login
    DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword, true);
    DemoAssertions.assertFormLoginSuccessful(mockMvc, internalUsername, userRawPassword, true);
    // incorrect user password
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, username, "invalid", true);
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, internalUsername, "invalid", true);
    // incorrect username
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, username + "1", userRawPassword, true);
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, internalUsername + "1", userRawPassword, true);
  }

  @Test
  void testInvalidExternalUserCannotLogon() throws Exception {
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, testDataGenerator.randomUsername() + "invalid", "invalid", true);
  }

  @Test
  void testExistingExternalUserCanLogon() throws Exception {
    final String username = testDataGenerator.randomUsername();
    final String userRawPassword = testDataGenerator.randomPassword();
    testDataGenerator.generateExternalUser(username, userRawPassword, true);

    final String otherUsername = testDataGenerator.randomUsername();
    final String otherUserRawPassword = testDataGenerator.randomPassword();
    testDataGenerator.generateExternalUser(otherUsername, otherUserRawPassword, true);

    // login and logout as first user
    DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword, true);
    DemoAssertions.assertFormLogoutSuccessful(mockMvc, true);
    // login and logout as second user
    DemoAssertions.assertFormLoginSuccessful(mockMvc, otherUsername, otherUserRawPassword, true);
    DemoAssertions.assertFormLogoutSuccessful(mockMvc, true);
  }

  @Test
  void testNonConcurrentSession() throws Exception {
    final String username = testDataGenerator.randomUsername();
    final String userRawPassword = testDataGenerator.randomPassword();
    final SecurityUser user = testDataGenerator.generateExternalUser(username, userRawPassword, true);

    var result = DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword, true);
    assertNotNull(result.getRequest().getSession());
    // use session details to perform a request to a secured endpoint for the same user's details
    var sessionRequestResult = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
            .secure(true)
            .session((MockHttpSession) result.getRequest().getSession())
            .param("userId", user.getId().toString()))
        .andExpect(status().isOk())
        .andReturn();
    var response = sessionRequestResult.getResponse();
    assertTrue(StringUtils.isNotBlank(response.getContentAsString()), "Expected response to be non-empty");
    DemoAssertions.assertFormLogoutSuccessful(mockMvc, true);
  }

  @Test
  void testConcurrentSessions() throws Exception {
    final String username = testDataGenerator.randomUsername();
    final String userRawPassword = testDataGenerator.randomPassword();
    final SecurityUser user = testDataGenerator.generateExternalUser(username, userRawPassword, true);

    var loginResult = DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword, true);
    var firstSession = loginResult.getRequest().getSession();
    assertNotNull(firstSession);
    mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
            .secure(true)
            .session((MockHttpSession) firstSession)
            .param("userId", user.getId().toString()))
        .andExpect(status().isOk())
        .andReturn();

    var secondLoginResult = DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword, true);
    var secondSession = secondLoginResult.getRequest().getSession();
    assertNotNull(secondSession);
    assertNotEquals(firstSession, secondSession);

    assertNotNull(secondSession.getId());
    mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
            .secure(true)
            .session((MockHttpSession) secondSession)
            .param("userId", user.getId().toString()))
        .andExpect(status().isOk())
        .andReturn();

    int retryCount = 2;
    MvcResult invalidSessionRequest = null;
    while (retryCount >= 0) {
      // A bit weird here, but it passes on second attempt...
      retryCount--;
      try {
        invalidSessionRequest = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
                .secure(true)
                .session((MockHttpSession) firstSession)
                .param("userId", user.getId().toString()))
            .andExpect(status().isUnauthorized())
            .andReturn();
      } catch (AssertionError e) {
        if (retryCount == 0) {
          fail("First user session should have been invalid but was not after " + retryCount + " attempts", e);
        }
      }
    }
    assertNotNull(invalidSessionRequest);
    var response = invalidSessionRequest.getResponse();
    assertTrue(StringUtils.isNotBlank(response.getContentAsString()), "Expected response to be non-empty");
  }
}
