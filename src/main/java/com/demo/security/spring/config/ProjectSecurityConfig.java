package com.demo.security.spring.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.demo.security.spring.authentication.AuthenticationAttemptManager;
import com.demo.security.spring.events.AuthenticationEvents;
import com.demo.security.spring.authentication.CustomAccessDeniedHandler;
import com.demo.security.spring.authentication.CustomBasicAuthenticationEntryPoint;
import com.demo.security.spring.controller.AccountController;
import com.demo.security.spring.controller.BalanceController;
import com.demo.security.spring.controller.CardsController;
import com.demo.security.spring.controller.ContactController;
import com.demo.security.spring.controller.LoansController;
import com.demo.security.spring.controller.UserController;
import com.demo.security.spring.controller.NoticesController;
import com.demo.security.spring.generate.AccountGenerator;
import com.demo.security.spring.generate.CardGenerator;
import com.demo.security.spring.generate.ContactMessagesFileGenerator;
import com.demo.security.spring.generate.LoanGenerator;
import com.demo.security.spring.generate.NoticeDetailsFileGenerator;
import com.demo.security.spring.generate.UserFileGenerator;
import com.demo.security.spring.repository.AuthenticationAttemptRepository;
import com.demo.security.spring.repository.ContactMessageRepository;
import com.demo.security.spring.repository.NoticeDetailsRepository;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.serialization.ZonedDateTimeDeserializer;
import com.demo.security.spring.serialization.ZonedDateTimeSerializer;
import com.demo.security.spring.service.ExampleDataGenerationService;
import com.demo.security.spring.service.InMemoryLoginService;
import com.demo.security.spring.service.JpaLoginService;
import com.demo.security.spring.service.LoginService;
import com.demo.security.spring.service.SpringDataJpaUserDetailsService;
import com.demo.security.spring.utils.Constants;
import com.demo.security.spring.utils.SpringProfileConstants;
import com.demo.security.spring.utils.StartupDatabasePopulator;
import com.demo.security.spring.service.ExampleDataManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer.SessionFixationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class ProjectSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, Environment environment) throws Exception {
    boolean isProd = environment.matchesProfiles(SpringProfileConstants.PRODUCTION);
    http.csrf().disable()
        .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
        .requiresChannel(rcc -> {
          // allow http for profiles other than 'prod', else allow only https
          if (isProd) {
            rcc.anyRequest().requiresSecure();
          } else {
            rcc.anyRequest().requiresInsecure();
          }
        })
        .sessionManagement(smc -> {
          // just fyi the view /invalidSession doesn't exist for now - so this is just an example config here
          smc.invalidSessionUrl("/invalidSession");
          // using default session fixation protection strategy of change session id
          smc.sessionFixation(SessionFixationConfigurer::changeSessionId);
          if (isProd) {
            smc.maximumSessions(1);
            // note that adding maxSessionsPreventsLogin would prevent additional logins once max sessions is reached
            // instead of default behavior which allows login and expires previous sessions
          }
        })
        .authorizeHttpRequests((requests) -> requests
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
            UserController.RESOURCE_PATH,
            UserController.RESOURCE_PATH + "/**"
        )
        .hasRole("ADMIN")
        .requestMatchers(
            NoticesController.NOTICES_RESOURCE_PATH,
            NoticesController.NOTICES_RESOURCE_PATH + "/**",
            ContactController.CONTACT_RESOURCE_PATH,
            ContactController.CONTACT_RESOURCE_PATH + "/**",
            "/invalidSession"
        )
        .permitAll()
    );
    http.formLogin(withDefaults());
    // configuration specific to http basic
    http.httpBasic(httpBasicConfigurer -> httpBasicConfigurer.authenticationEntryPoint(
        new CustomBasicAuthenticationEntryPoint(objectMapper(), environment, isProd)));
    http.exceptionHandling(customizer -> customizer.accessDeniedHandler(new CustomAccessDeniedHandler(objectMapper(), isProd)));
    // global config for authenticationEntryPoint is possible using
    // http.exceptionHandling(customizer -> customizer.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint(objectMapper(), environment, isProd))));
    return http.build();
  }

  private CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(Constants.EXAMPLE_ALLOWED_CORS_PATHS));
    configuration.setAllowedMethods(Collections.singletonList("*"));
    configuration.setAllowedHeaders(Collections.singletonList("*"));
    configuration.setAllowCredentials(true); // required for logins from allowedOrigins
    configuration.setMaxAge(3600L); // how long browser caches the CORS details - as browser makes a pre-flight OPTIONS request for CORS config
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /**
   * <em>For demo/dev-environment purposes only</em>
   * <p>Seeds a number of users from a local csv file into an in-memory user details manager</p>
   *
   * @return an in memory user details manager which is only safe for local demo or sample
   * applications
   */
  @Bean(name = "userDetailsService")
  @Profile(SpringProfileConstants.IN_MEMORY_USERS)
  public UserDetailsService inMemoryUserDetailsManager(final ExampleDataManager exampleDataManager) {
    return new InMemoryUserDetailsManager(exampleDataManager.getInMemoryUsers());
  }

  /**
   * Create a jdbc user details manager. Note that the docker-compose file and
   * spring-boot-docker-compose by default start a postgres and adminer container.
   *
   * @param repository - a spring data jpa repository
   * @return JdbcUserDetailsManager
   */
  @Bean(name = "userDetailsService")
  @Profile("! " + SpringProfileConstants.IN_MEMORY_USERS)
  public UserDetailsService jpaUserDetailsService(final SecurityUserRepository repository) {
    return SpringDataJpaUserDetailsService
        .builder()
        .securityUserRepository(repository)
        .build();
  }

  /**
   * For development environment usage only.
   * Populates the database with test users defined in a json resource file.
   * @param userRepository the {@link SecurityUserRepository} for interacting with postgres db
   * @param noticeDetailsRepository the {@link NoticeDetailsRepository} for interacting with postgres db
   * @param contactMessageRepository the {@link ContactMessageRepository} for interacting with postgres db
   * @return StartupDatabasePopulator
   */
  @Bean
  @Profile("! " + SpringProfileConstants.IN_MEMORY_USERS + " && " + SpringProfileConstants.POSTGRES)
  public StartupDatabasePopulator startupDatabasePopulator(
      final SecurityUserRepository userRepository,
      final NoticeDetailsRepository noticeDetailsRepository,
      final ContactMessageRepository contactMessageRepository,
      final ExampleDataManager exampleDataManager
  ) {
    return StartupDatabasePopulator.builder()
        .exampleDataManager(exampleDataManager)
        .securityUserRepository(userRepository)
        .noticeDetailsRepository(noticeDetailsRepository)
        .contactMessageRepository(contactMessageRepository)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean(name = "loginService")
  @Profile(SpringProfileConstants.IN_MEMORY_USERS)
  public LoginService inMemoryLoginService(UserDetailsService userDetailsService) {
    if (!(userDetailsService instanceof InMemoryUserDetailsManager)) {
      throw new RuntimeException("Provided userDetailsService was expected to be InMemoryUserDetailsManager but was "
          + userDetailsService.getClass().getName());
    }
    return InMemoryLoginService.builder().userDetailsService(userDetailsService).build();
  }

  @Bean(name = "loginService")
  @Profile("! " + SpringProfileConstants.IN_MEMORY_USERS)
  public LoginService jpaLoginService(final SecurityUserRepository securityUserRepository, final PasswordEncoder passwordEncoder) {
    return JpaLoginService.builder()
        .securityUserRepository(securityUserRepository)
        .passwordEncoder(passwordEncoder)
        .build();
  }

  /**
   * The examples users manager handles retrieving example users from resource files
   * for development and testing environments
   * @return an example users manager
   */
  @Bean
  public ExampleDataManager exampleDataManager(
      final ExampleDataGenerationService generationService,
      @Value("${example-data.regenerate:false}") boolean regenerateData
  ) {
    return ExampleDataManager.builder()
        .passwordEncoder(passwordEncoder())
        .objectMapper(objectMapper())
        .generationService(generationService)
        .regenerateData(regenerateData)
        .build();
  }

  @Bean
  public Faker faker() {
    return new Faker();
  }

  @Bean
  public ObjectMapper objectMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    // add module to serialize and deserialize ZonedDateTime as string instead of instant
    final SimpleModule customModule = new SimpleModule("security-demo-module");
    customModule.addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer(ZonedDateTime.class));
    customModule.addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(ZonedDateTime.class));
    objectMapper.registerModule(customModule);
    return objectMapper;
  }

  @Bean
  public CardGenerator cardGenerator(@Value("${example-data.cards.count:20}") int cardCount) {
    return new CardGenerator(faker(), objectMapper(), cardCount);
  }

  @Bean
  public LoanGenerator loanGenerator(@Value("${example-data.loan.count:20}") int loanCount) {
    return new LoanGenerator(faker(), objectMapper(), loanCount);
  }

  @Bean
  public NoticeDetailsFileGenerator noticeDetailsFileGenerator(@Value("${example-data.notice.count:20}") int noticeCount) {
    final NoticeDetailsFileGenerator noticeDetailsFileGenerator = new NoticeDetailsFileGenerator(faker(), objectMapper());
    noticeDetailsFileGenerator.setItemCount(noticeCount);
    return noticeDetailsFileGenerator;
  }

  @Bean
  public ContactMessagesFileGenerator contactMessagesFileGenerator(@Value("${example-data.message.count:20}") int messageCount) {
    final ContactMessagesFileGenerator contactMessagesFileGenerator = new ContactMessagesFileGenerator(faker(), objectMapper());
    contactMessagesFileGenerator.setItemCount(messageCount);
    return contactMessagesFileGenerator;
  }

  @Bean
  public AccountGenerator accountGenerator(@Value("${example-data.account.count:1}") int accountCount) {
    return new AccountGenerator(faker(), objectMapper(), accountCount);
  }

  @Bean
  public UserFileGenerator userFileGenerator(
      final LoanGenerator loanGenerator,
      final AccountGenerator accountGenerator,
      final CardGenerator cardGenerator,
      @Value("${example-data.user.count:20}") int userCount
  ) {
    final UserFileGenerator userFileGenerator = new UserFileGenerator(faker(), objectMapper());
    userFileGenerator.setLoanGenerator(loanGenerator);
    userFileGenerator.setAccountGenerator(accountGenerator);
    userFileGenerator.setCardGenerator(cardGenerator);
    userFileGenerator.setItemCount(userCount);
    return userFileGenerator;
  }

  @Bean
  public ExampleDataGenerationService exampleDataGenerationService(
      UserFileGenerator userFileGenerator,
      NoticeDetailsFileGenerator noticeDetailsFileGenerator,
      ContactMessagesFileGenerator contactMessagesFileGenerator
  ) {
    return ExampleDataGenerationService.builder()
        .userFileGenerator(userFileGenerator)
        .noticeDetailsFileGenerator(noticeDetailsFileGenerator)
        .contactMessagesFileGenerator(contactMessagesFileGenerator)
        .build();
  }

  @Bean
  public AuthenticationEvents authenticationEventListeners() {
    return new AuthenticationEvents();
  }

  @Bean
  public AuthenticationAttemptManager authenticationAttemptManager(AuthenticationAttemptRepository attemptRepository) {
    return new AuthenticationAttemptManager().setAttemptRepository(attemptRepository);
  }
}
