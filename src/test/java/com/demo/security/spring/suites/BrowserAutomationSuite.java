package com.demo.security.spring.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * A suite of integration tests using playwright for browser automation.
 * The application must be running for these tests to run.
 *
 * To run this test suite, start the application and then execute command:
 * mvn clean verify -DrunSuite=com.demo.security.spring.suites.BrowserAutomationSuite
 */
@Suite
@SuiteDisplayName("Playwright Browser Automation Test Suite")
@SelectPackages("com.demo.security.spring.playwright")
public class BrowserAutomationSuite {

}
