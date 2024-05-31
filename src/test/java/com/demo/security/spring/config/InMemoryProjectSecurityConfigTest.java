package com.demo.security.spring.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
// can also turn off org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration if hibernate / jpa is added in
@ActiveProfiles(value = ProjectSecurityConfig.PROFILE_IN_MEMORY_USERS)
class InMemoryProjectSecurityConfigTest {

    @Autowired
    UserDetailsManager userDetailsManager;

    @Test
    void inMemoryUserDetailsManager() {
        assertInstanceOf(InMemoryUserDetailsManager.class, userDetailsManager);
    }
}