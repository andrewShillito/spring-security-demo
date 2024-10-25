package com.demo.security.spring.playwright;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.demo.security.spring.model.SecurityGroup;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.Constants;
import com.demo.security.spring.utils.CookieNames;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import org.junit.platform.commons.util.StringUtils;

public class PlaywrightTestUtils {

  /**
   * Return the sessionId from the cookie with name {@link CookieNames#COOKIE_SESSION_ID}
   * or null if none
   * @param context the browser context
   * @return the session id cookie value
   */
  public static String getSessionId(BrowserContext context) {
    Cookie cookie = getCookie(context, CookieNames.COOKIE_SESSION_ID);
    if (cookie != null) {
      return cookie.value;
    }
    return null;
  }

  /**
   * Asserts that the browser context has a session id cookie
   * @param context the browser context
   */
  public static void assertHasSessionId(BrowserContext context) {
    assertNotNull(getCookie(context, CookieNames.COOKIE_SESSION_ID));
  }

  /**
   * Return the csrf token from the cookie with name {@link CookieNames#COOKIE_XSRF_TOKEN}
   * or null if none
   * @param context the browser context
   * @return the csrf token cookie value
   */
  public static String getCsrfToken(BrowserContext context) {
    Cookie cookie = getCookie(context, CookieNames.COOKIE_XSRF_TOKEN);
    if (cookie != null) {
      return cookie.value;
    }
    return null;
  }

  /**
   * Asserts that the browser context has a csrf token cookie
   * @param context the browser context
   */
  public static void assertHasCsrfToken(BrowserContext context) {
    assertNotNull(getCookie(context, CookieNames.COOKIE_XSRF_TOKEN));
  }

  /**
   * Return the cookie in the provided browser context which matches the provided
   * name or null if no matching cookies exist
   * @param context the browser context
   * @param name the cookie name to locate
   * @return the cookie object or null
   */
  public static Cookie getCookie(BrowserContext context, String name) {
    return context.cookies().stream().filter(it -> name.equals(it.name)).findFirst().orElse(null);
  }

  /**
   * Return the {@link SecurityGroup} matching the given group code or null if none
   * @param user the security user to search the groups for
   * @param code the code for the security group
   * @return SecurityGroup or null if none match or null or empty input params
   */
  public static SecurityGroup getSecurityGroup(SecurityUser user, String code) {
    if (StringUtils.isNotBlank(code) && user != null && !user.getGroups().isEmpty()) {
      return user.getGroups()
          .stream()
          .filter(it -> code.equals(it.getCode()))
          .findFirst()
          .orElse(null);
    }
    return null;
  }

  public static void logon(Page page, String username, String password) {
    final String loginPath = "login";
    page.navigate(loginPath);
    assertThat(page).hasURL(loginPath);
    final Locator usernameLocator = page.locator("input#username");
    final Locator passwordLocator = page.locator("input#password");
    final Locator submitButton = page.locator("button");

    // enter data
    usernameLocator.click();
    usernameLocator.fill(username);
    passwordLocator.click();
    passwordLocator.fill(password);

    String sessionId = PlaywrightTestUtils.getSessionId(page.context());
    assertNotNull(sessionId);
    String csrfToken = PlaywrightTestUtils.getCsrfToken(page.context());
    assertNotNull(csrfToken);
    submitButton.click();

    // default redirect url is to swagger ui
    assertThat(page).hasURL(Constants.SWAGGER_UI_URL);
  }
}
