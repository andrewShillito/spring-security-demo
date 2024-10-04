package com.demo.security.spring.playwright;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.demo.security.spring.utils.Constants;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

/**
 * Browser automation integration login test which requires the application to be running
 */
@UsePlaywright(value = PlaywrightOptions.class)
public class BrowserLoginTest {

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
  }

}
