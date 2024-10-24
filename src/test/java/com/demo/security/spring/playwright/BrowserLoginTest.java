package com.demo.security.spring.playwright;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.config.ProjectSecurityConfig;
import com.demo.security.spring.controller.UserController;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Browser automation integration login test which requires the application to be running
 */
@UsePlaywright(value = PlaywrightOptions.class)
public class BrowserLoginTest {

  private static final ObjectMapper objectMapper = new ProjectSecurityConfig().objectMapper();

  @Test
  void testLoginBasic(Page page) {
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
    usernameLocator.fill("user");
    passwordLocator.click();
    passwordLocator.fill("password");

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
    // TODO: add more testing of resulting groups etc...
  }

}
