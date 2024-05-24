package com.demo.security.spring.config;

import com.demo.security.spring.controller.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
}
