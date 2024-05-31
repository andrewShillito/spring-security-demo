package com.demo.security.spring.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles(value = "inMemoryUsers")
class InMemoryProjectSecurityConfigTest {

    @Autowired
    UserDetailsManager userDetailsManager;

    @Test
    void inMemoryUserDetailsManager() {
        assertInstanceOf(InMemoryUserDetailsManager.class, userDetailsManager);
    }
}