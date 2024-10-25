package com.demo.security.spring.playwright;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.config.ProjectSecurityConfig;
import com.demo.security.spring.controller.UserController;
import com.demo.security.spring.controller.error.AuthErrorDetailsResponse;
import com.demo.security.spring.generate.UserGenerator;
import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityGroup;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.AuthorityGroups;
import com.demo.security.spring.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import com.google.common.collect.Sets;

/**
 * Browser automation integration login test which requires the application to be running
 */
@UsePlaywright(value = PlaywrightOptions.class)
public class BrowserLoginTest {

  private static final ObjectMapper objectMapper = new ProjectSecurityConfig().objectMapper();

  @Test
  void testLoginExampleUser(Page page) {
    final String loginPath = "login";
    page.navigate(loginPath);
    assertThat(page).hasURL(loginPath);
    assertThat(page).hasTitle("Please sign in");
    final Locator usernameLocator = page.locator("input#username");
    assertThat(usernameLocator).isEmpty();
    assertThat(usernameLocator).isVisible();
    assertThat(usernameLocator).isEditable();
    final Locator passwordLocator = page.locator("input#password");
    assertThat(passwordLocator).isEmpty();
    assertThat(passwordLocator).isVisible();
    assertThat(passwordLocator).isEditable();
    final Locator submitButton = page.locator("button");
    assertThat(submitButton).isVisible();
    assertThat(submitButton).isEditable();

    // enter data
    usernameLocator.click();
    usernameLocator.fill(UserGenerator.EXAMPLE_USERNAME_USER);
    passwordLocator.click();
    passwordLocator.fill(UserGenerator.DEFAULT_TESTING_PASSWORD);

    String sessionId = PlaywrightTestUtils.getSessionId(page.context());
    assertNotNull(sessionId);
    String csrfToken = PlaywrightTestUtils.getCsrfToken(page.context());
    assertNotNull(csrfToken);
    submitButton.click();

    // default redirect url is to swagger ui
    assertThat(page).hasURL(Constants.SWAGGER_UI_URL);
    // asserts that we can reach the swagger schema page
    Page swaggerSchemaPage = page.context().waitForPage(() -> {
      page.locator("#swagger-ui div.information-container.wrapper a").click(); // opens a new tab
    });
    assertThat(swaggerSchemaPage).hasURL(Constants.SWAGGER_SCHEMA_URL);

    PlaywrightTestUtils.assertHasSessionId(page.context());
    PlaywrightTestUtils.assertHasCsrfToken(page.context());

    String newSessionId = PlaywrightTestUtils.getSessionId(page.context());
    String newCsrfToken = PlaywrightTestUtils.getCsrfToken(page.context());

    assertNotEquals(sessionId, newSessionId);
    assertNotEquals(csrfToken, newCsrfToken);

    APIResponse response = page.request().get(UserController.RESOURCE_PATH);
    assertEquals(HttpStatus.OK.value(), response.status());
    String body = new String(response.body());
    DemoAssertions.assertNotEmpty(body);

    SecurityUser user = null;
    try {
      user = objectMapper.readValue(body, SecurityUser.class);
    } catch (IOException e) {
      fail("Unable to map get /user to SecurityUser type with error", e);
    }
    assertNotNull(user);
    assertEquals("user", user.getUsername());
    assertNull(user.getPassword());
    assertNotNull(user.getGroups());
    assertNotNull(user.getAuthorities());
    assertNotNull(user.getSecurityAuthorities());
    assertTrue(user.getSecurityAuthorities().isEmpty());
    assertEquals(2, user.getGroups().size());
    SecurityGroup groupUser = PlaywrightTestUtils.getSecurityGroup(user, AuthorityGroups.GROUP_USER);
    assertNotNull(groupUser);
    assertNotNull(groupUser.getAuthorities());
    assertEquals(4, groupUser.getAuthorities().size());
    SecurityGroup groupAccountHolder = PlaywrightTestUtils.getSecurityGroup(user, AuthorityGroups.GROUP_ACCOUNT_HOLDER);
    assertNotNull(groupAccountHolder);
    assertNotNull(groupAccountHolder.getAuthorities());
    assertEquals(16, groupAccountHolder.getAuthorities().size());
    assertEquals(16, user.getAuthorities().size());
    assertEquals(16, Sets.union(groupUser.getAuthorities(), groupAccountHolder.getAuthorities()).size());
    Set<String> authorityNames = user.getAuthorities().stream().map(SecurityAuthority::getAuthority).collect(Collectors.toSet());
    DemoAssertions.assertSetsEqual(authorityNames, AuthorityGroups.GROUP_ACCOUNT_HOLDER_AUTHS);

    APIResponse actuatorResponse = page.request().get("/actuator");
    assertFalse(actuatorResponse.ok());
    assertEquals(HttpStatus.FORBIDDEN.value(), actuatorResponse.status());
    try {
      var errorResponse = objectMapper.readValue(actuatorResponse.body(), AuthErrorDetailsResponse.class);
      assertEquals("/actuator", errorResponse.getRequestUri());
      DemoAssertions.assertDateIsNowIsh(errorResponse.getTime());
      assertEquals("Access Denied", errorResponse.getErrorMessage());
      assertEquals("Example additional info", errorResponse.getAdditionalInfo());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testLoginSystemAdmin(Page page) {
    PlaywrightTestUtils.logon(page, UserGenerator.EXAMPLE_USERNAME_SYSTEM_ADMIN, UserGenerator.DEFAULT_TESTING_PASSWORD);
    APIResponse actuatorResponse = page.request().get("/actuator");
    assertTrue(actuatorResponse.ok());
    assertEquals(HttpStatus.OK.value(), actuatorResponse.status());
  }



}
