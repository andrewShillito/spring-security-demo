package com.demo.security.spring.config;

import com.demo.security.spring.controller.*;
import com.demo.security.spring.utils.SeedUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class ProjectSecurityConfig {

    /** A spring profile which uses an in memory user details service - disables docker-compose startup of postgres and adminer */
    public static final String PROFILE_IN_MEMORY_USERS = "inMemoryUsers";

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
    @Bean(name = "userDetailsService")
    @Profile(PROFILE_IN_MEMORY_USERS)
    public UserDetailsService inMemoryUserDetailsManager() {
        return new InMemoryUserDetailsManager(SeedUtils.getInMemoryUsers());
    }

    /**
     * Create a jdbc user details manager. Note that the docker-compose file and spring-boot-docker-compose
     * by default start a postgres and adminer container.
     * @param dataSource
     * @return JdbcUserDetailsManager
     */
    @Bean(name = "userDetailsService")
    @Profile("! " + PROFILE_IN_MEMORY_USERS)
    public UserDetailsService jdbcUserDetailsManager(final DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }
}
