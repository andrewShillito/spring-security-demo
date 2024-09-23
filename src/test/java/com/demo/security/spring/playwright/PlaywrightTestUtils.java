package com.demo.security.spring.playwright;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.Cookie;

public class PlaywrightTestUtils {

  /** The expected cookie name for the session id */
  public static final String COOKIE_SESSION_ID = "JSESSIONID";
  /** The expected cookie name for the csrf token */
  public static final String COOKIE_XSRF_TOKEN = "XSRF-TOKEN";

  /**
   * Return the sessionId from the cookie with name {@link #COOKIE_SESSION_ID}
   * or null if none
   * @param context the browser context
   * @return the session id cookie value
   */
  public static String getSessionId(BrowserContext context) {
    Cookie cookie = getCookie(context, COOKIE_SESSION_ID);
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
    assertNotNull(getCookie(context, COOKIE_SESSION_ID));
  }

  /**
   * Return the csrf token from the cookie with name {@link #COOKIE_XSRF_TOKEN}
   * or null if none
   * @param context the browser context
   * @return the csrf token cookie value
   */
  public static String getCsrfToken(BrowserContext context) {
    Cookie cookie = getCookie(context, COOKIE_XSRF_TOKEN);
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
    assertNotNull(getCookie(context, COOKIE_XSRF_TOKEN));
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
}
