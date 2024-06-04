package com.demo.security.spring.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SpringBootTest
class JdbcProjectSecurityConfigTest {

    @Autowired
    UserDetailsManager userDetailsManager;

    @Test
    void jdbcUserDetailsManager() {
        assertInstanceOf(JdbcUserDetailsManager.class, userDetailsManager);
    }
}