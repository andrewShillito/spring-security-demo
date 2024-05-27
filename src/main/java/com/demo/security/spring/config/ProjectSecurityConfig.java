package com.demo.security.spring.config;

import com.demo.security.spring.controller.*;
import com.demo.security.spring.utils.SeedUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class ProjectSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception  {
        http.authorizeHttpRequests((requests) -> requests
                .requestMatchers(
                    AccountController.ACCOUNT_RESOURCE_PATH,
                    AccountController.ACCOUNT_RESOURCE_PATH + "/**",
                    BalanceController.BALANCE_RESOURCE_PATH,
                    BalanceController.BALANCE_RESOURCE_PATH + "/**",
                    CardsController.CARDS_RESOURCE_PATH,
                    CardsController.CARDS_RESOURCE_PATH + "/**",
                    LoansController.LOANS_RESOURCE_PATH,
                    LoansController.LOANS_RESOURCE_PATH + "/**")
                .authenticated()
                .requestMatchers(
                       NoticesController.NOTICES_RESOURCE_PATH,
                       NoticesController.NOTICES_RESOURCE_PATH + "/**",
                       ContactController.CONTACT_RESOURCE_PATH,
                       ContactController.CONTACT_RESOURCE_PATH + "/**"
                )
                .permitAll()
        );
        http.formLogin(withDefaults());
        http.httpBasic(withDefaults());
        return http.build();
    }

    /**
     * <em>For demo/dev-environment purposes only</em>
     * <p>Seeds a number of users from a local csv file into an in-memory user details manager</p>
     * @return an in memory user details manager which is only safe for local demo or sample applications
     */
    @Bean
    @ConditionalOnProperty(value = "inMemory.user.details.manager", havingValue = "true")
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        return new InMemoryUserDetailsManager(SeedUtils.getInMemoryUsers());
    }
}
