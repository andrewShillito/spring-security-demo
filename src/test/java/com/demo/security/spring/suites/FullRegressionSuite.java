package com.demo.security.spring.suites;

import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Runs all JUnit tests except browser automation tests using playwright
 * Playwright tests can be run using: {@link BrowserAutomationSuite} but app must be running.
 */
@Suite
@SuiteDisplayName("Full Regression Test Suite")
@SelectPackages("com.demo.security.spring")
@ExcludePackages({ "com.demo.security.spring.suites", "com.demo.security.spring.playwright" })
public class FullRegressionSuite {
}
