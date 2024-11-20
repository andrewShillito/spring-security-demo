package com.demo.security.spring.authentication;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.controller.LoansController;
import com.demo.security.spring.model.SecurityUser;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testing for dev/test environment form login
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FormLoginDevTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected TestDataGenerator testDataGenerator;

  @Test
  void testFormLoginIsAvailable() throws Exception {
    // note that this is http form not https without prod profile
    mockMvc.perform(get("/login"))
        .andExpect(status().isOk());
  }

  @Test
  void testInvalidLogons() throws Exception {
    // non-existent user
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, testDataGenerator.randomUsername() + "invalid", "invalid");
    // invalid external user password
    final String username = testDataGenerator.randomUsername();
    final String adminUsername = username + "internal";
    final String userRawPassword = testDataGenerator.randomPassword();
    testDataGenerator.generateExternalUser(username, userRawPassword, true);
    testDataGenerator.generateAdminUser(adminUsername, userRawPassword, true);
    // correct login
    DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword);
    DemoAssertions.assertFormLoginSuccessful(mockMvc, adminUsername, userRawPassword);
    // incorrect user password
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, username, "invalid");
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, adminUsername, "invalid");
    // incorrect username
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, username + "invalid", userRawPassword);
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, adminUsername + "invalid", userRawPassword);
  }

  @Test
  void testExternalUserCanLogon() throws Exception {
    final String username = testDataGenerator.randomUsername();
    final String userRawPassword = testDataGenerator.randomPassword();
    testDataGenerator.generateExternalUser(username, userRawPassword, true);

    final String otherUsername = testDataGenerator.randomUsername();
    final String otherUserRawPassword = testDataGenerator.randomPassword();
    testDataGenerator.generateExternalUser(otherUsername, otherUserRawPassword, true);

    // login and logout as first user
    DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword);
    DemoAssertions.assertFormLogoutSuccessful(mockMvc);
    // login and logout as second user
    DemoAssertions.assertFormLoginSuccessful(mockMvc, otherUsername, otherUserRawPassword);
    DemoAssertions.assertFormLogoutSuccessful(mockMvc);
  }

  @Test
  void testNonConcurrentSession() throws Exception {
    final String username = testDataGenerator.randomUsername();
    final String userRawPassword = testDataGenerator.randomPassword();
    final SecurityUser user = testDataGenerator.generateExternalUser(username, userRawPassword, true);

    mockMvc.perform(get(LoansController.RESOURCE_PATH))
        .andExpect(status().isUnauthorized())
        .andExpect(unauthenticated());

    var result = DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword);
    assertNotNull(result.getRequest().getSession());
    // use session details to perform a request to a secured endpoint for the same user's details
    var sessionRequestResult = mockMvc.perform(get(LoansController.RESOURCE_PATH)
            .session((MockHttpSession) result.getRequest().getSession()))
        .andExpect(status().isOk())
        .andReturn();
    var response = sessionRequestResult.getResponse();
    assertTrue(StringUtils.isNotBlank(response.getContentAsString()), "Expected response to be non-empty");
    DemoAssertions.assertFormLogoutSuccessful(mockMvc);
  }

  @Test
  void testConcurrentSessions() throws Exception {
    final String username = testDataGenerator.randomUsername();
    final String userRawPassword = testDataGenerator.randomPassword();
    final SecurityUser user = testDataGenerator.generateExternalUser(username, userRawPassword, true);

    mockMvc.perform(get(LoansController.RESOURCE_PATH))
        .andExpect(status().isUnauthorized())
        .andExpect(unauthenticated());

    var loginResult = DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword);
    var firstSession = loginResult.getRequest().getSession();
    assertNotNull(firstSession);

    var secondLoginResult = DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword);
    var secondSession = secondLoginResult.getRequest().getSession();
    assertNotNull(secondSession);
    assertNotEquals(firstSession, secondSession);

    MvcResult sessionRequestResult = mockMvc.perform(get(LoansController.RESOURCE_PATH)
            .session((MockHttpSession) firstSession))
        .andExpect(status().isOk())
        .andReturn();
    var response = sessionRequestResult.getResponse();
    assertTrue(StringUtils.isNotBlank(response.getContentAsString()), "Expected response to be non-empty");

    // and again for good measure
    mockMvc.perform(get(LoansController.RESOURCE_PATH)
            .session((MockHttpSession) firstSession))
        .andExpect(status().isOk())
        .andReturn();
  }
}
