package com.demo.security.spring.authentication;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.controller.LoansController;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.SpringProfileConstants;
import jakarta.servlet.http.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
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

  @Autowired
  private TestRestTemplate testRestTemplate;

  // TODO: add testing for:
  //  concurrent sessions using TestRestTemplate
  //  invalid external user fails logon
  //  internal user can long
  //  invalid internal user fails long

  @Test
  void testFormLoginIsAvailable() throws Exception {
    mockMvc.perform(get("/login"))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl("https://localhost/login"));
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
    // TODO: use testRestTemplate to handle JSESSIONID cookie directly
    //  in order to assert concurrent sessions > 1 are disallowed
    final String username = testDataGenerator.randomUsername();
    final String userRawPassword = testDataGenerator.randomPassword();
    final SecurityUser user = testDataGenerator.generateExternalUser(username, userRawPassword, true);

    var loginResult = DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword, true);
    var firstSession = loginResult.getRequest().getSession();
    assertNotNull(firstSession);
    mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
            .secure(true)
            .cookie(new Cookie("JSESSIONID", firstSession.getId())) // note firstSession.getId is not a JSESSIONID cookie value
            .param("userId", user.getId().toString()))
        .andExpect(status().isOk())
        .andReturn();

    var secondLoginResult = DemoAssertions.assertFormLoginSuccessful(mockMvc, username, userRawPassword, true);
    var secondSession = secondLoginResult.getRequest().getSession();
    assertNotNull(secondSession);
    assertNotEquals(firstSession, secondSession);

    assertNotNull(secondSession.getId());
    MvcResult sessionRequestResult = mockMvc.perform(get(LoansController.LOANS_RESOURCE_PATH)
            .secure(true)
            .cookie(new Cookie("JSESSIONID", firstSession.getId()))
            .param("userId", user.getId().toString()))
          .andExpect(status().isUnauthorized())
          .andReturn();
    var response = sessionRequestResult.getResponse();
    assertTrue(StringUtils.isNotBlank(response.getContentAsString()), "Expected response to be non-empty");
  }
}
