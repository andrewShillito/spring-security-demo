package com.demo.security.spring.suites;

import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Full Regression Test Suite")
@SelectPackages("com.demo.security.spring")
@ExcludePackages("com.demo.security.spring.suites")
public class FullRegressionSuite {
}
