package com.demo.security.spring.playwright;

import org.apache.commons.lang3.StringUtils;

public class PlaywrightUtils {

  /** A property which can be used to set the server base url - default is localhost:8080 */
  public static final String PROPERTY_SERVER_BASE_URL = "playwright.application.base-url";
  /** A property to use to set whether the app is using https or http - default is http */
  public static final String PROPERTY_SERVER_IS_HTTPS = "playwright.application.is-https";
  /** A property to use to set whether playwright should show the browser or not during automation tests */
  public static final String PROPERTY_PLAYWRIGHT_HEADLESS = "playwright.headless";

  public static String applicationBaseUrl() {
    return getOrDefaultProperty(PROPERTY_SERVER_BASE_URL, "localhost:8080");
  }

  public static boolean isHttps() {
    return getOrDefaultProperty(PROPERTY_SERVER_IS_HTTPS, false);
  }

  public static boolean isHeadless() {
    return getOrDefaultProperty(PROPERTY_PLAYWRIGHT_HEADLESS, false);
  }

  public static String getOrDefaultProperty(String name, String defaultValue) {
    String property = System.getenv(name);
    if (StringUtils.isBlank(property)) {
      property = defaultValue;
    }
    return property;
  }

  public static boolean getOrDefaultProperty(String name, boolean defaultValue) {
    String property = System.getenv(name);
    if (StringUtils.isBlank(property)) {
      return defaultValue;
    }
    return Boolean.parseBoolean(property);
  }

  public static String appPath(String path) {
    if (StringUtils.isBlank(path)) {
      path = "";
    } else if (!StringUtils.startsWith(path, "/")) {
      path = "/" + path;
    }
    return applicationBaseUrl() + path;
  }

  public static String appPathFull() {
    return appPathFull(null);
  }

  public static String appPathFull(String path) {
    return isHttps() ? "https://" + appPath(path) : "http://" + appPath(path);
  }

}
