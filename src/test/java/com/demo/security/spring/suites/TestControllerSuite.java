package com.demo.security.spring.suites;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Controller Test Suite")
@SelectPackages("com.demo.security.spring.controller")
public class TestControllerSuite {

}
