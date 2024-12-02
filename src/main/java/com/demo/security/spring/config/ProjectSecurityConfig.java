package com.demo.security.spring.config;

import com.demo.security.spring.authentication.AuthenticationAttemptManager;
import com.demo.security.spring.authentication.CustomAuthenticationFailureHandler;
import com.demo.security.spring.authentication.CustomAuthenticationSuccessHandler;
import com.demo.security.spring.controller.RegisterController;
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
import com.demo.security.spring.events.AuthorizationEvents;
import com.demo.security.spring.filter.CsrfCookieFilter;
import com.demo.security.spring.generate.AccountGenerator;
import com.demo.security.spring.generate.CardGenerator;
import com.demo.security.spring.generate.ContactMessageGenerator;
import com.demo.security.spring.generate.LoanGenerator;
import com.demo.security.spring.generate.NoticeDetailsGenerator;
import com.demo.security.spring.generate.UserGenerator;
import com.demo.security.spring.repository.AccountRepository;
import com.demo.security.spring.repository.AccountTransactionRepository;
import com.demo.security.spring.repository.AuthenticationAttemptRepository;
import com.demo.security.spring.repository.CardRepository;
import com.demo.security.spring.repository.ContactMessageRepository;
import com.demo.security.spring.repository.LoanRepository;
import com.demo.security.spring.repository.NoticeDetailsRepository;
import com.demo.security.spring.repository.SecurityAuthorityRepository;
import com.demo.security.spring.repository.SecurityGroupAuthorityRepository;
import com.demo.security.spring.repository.SecurityGroupRepository;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.serialization.ZonedDateTimeDeserializer;
import com.demo.security.spring.serialization.ZonedDateTimeSerializer;
import com.demo.security.spring.service.AccountService;
import com.demo.security.spring.service.AccountServiceImpl;
import com.demo.security.spring.service.BalanceService;
import com.demo.security.spring.service.BalanceServiceImpl;
import com.demo.security.spring.service.SecurityUserServiceImpl;
import com.demo.security.spring.service.CardService;
import com.demo.security.spring.service.CardServiceImpl;
import com.demo.security.spring.service.ExampleDataGenerationService;
import com.demo.security.spring.service.LoanService;
import com.demo.security.spring.service.LoanServiceImpl;
import com.demo.security.spring.service.SecurityUserService;
import com.demo.security.spring.service.SecurityUserValidationService;
import com.demo.security.spring.service.SecurityUserValidationServiceImpl;
import com.demo.security.spring.service.UserAuthorityManager;
import com.demo.security.spring.service.UserCache;
import com.demo.security.spring.service.UserDetailsManagerImpl;
import com.demo.security.spring.utils.AuthorityAdminPrivileges;
import com.demo.security.spring.utils.AuthorityUserPrivileges;
import com.demo.security.spring.utils.Constants;
import com.demo.security.spring.utils.SpringProfileConstants;
import com.demo.security.spring.service.StartupDatabasePopulator;
import com.demo.security.spring.service.ExampleDataManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.cache.CacheBuilder;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer.SessionFixationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableCaching /* see https://medium.com/simform-engineering/spring-boot-caching-with-redis-1a36f719309f */
public class ProjectSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, Environment environment) throws Exception {
    boolean isProd = environment.matchesProfiles(SpringProfileConstants.PRODUCTION);
    http.securityContext(contextConfigurer -> contextConfigurer.requireExplicitSave(false))
        .cors(customizer -> customizer.configurationSource(corsConfigurationSource()))
        .requiresChannel(rcc -> {
          // allow http for profiles other than 'prod', else allow only https
          if (isProd) {
            rcc.anyRequest().requiresSecure();
          } else {
            rcc.anyRequest().requiresInsecure();
          }
        })
        .csrf(csrfConfigurer -> csrfConfigurer
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            .ignoringRequestMatchers(ContactController.RESOURCE_PATH + "/**")) // RegisterController.RESOURCE_PATH + "/**"
        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
        .sessionManagement(smc -> {
          smc.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
          // just fyi the view /invalidSession doesn't exist for now - so this is just an example config here
          smc.invalidSessionUrl(Constants.INVALID_SESSION_URL);
          // using default session fixation protection strategy of change session id
          smc.sessionFixation(SessionFixationConfigurer::changeSessionId);
          if (isProd) {
            smc.maximumSessions(1);
            // note that adding maxSessionsPreventsLogin would prevent additional logins once max sessions is reached
            // instead of default behavior which allows login and expires previous sessions
          }
        })
        .authorizeHttpRequests((requests) -> requests
             // account controller endpoints
            .requestMatchers(HttpMethod.GET, AccountController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_ACCOUNT_VIEW, AuthorityAdminPrivileges.AUTH_ADMIN_ACCOUNTS_VIEW)
            .requestMatchers(HttpMethod.POST, AccountController.RESOURCE_PATH + "/**")
            .hasAuthority(AuthorityUserPrivileges.AUTH_SELF_ACCOUNT_APPLY)
            .requestMatchers(HttpMethod.PATCH, AccountController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_ACCOUNT_EDIT, AuthorityAdminPrivileges.AUTH_ADMIN_ACCOUNTS_EDIT)
            // account transaction endpoints
            .requestMatchers(HttpMethod.GET, BalanceController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_TRANSACTION_VIEW, AuthorityAdminPrivileges.AUTH_ADMIN_TRANSACTIONS_VIEW)
            .requestMatchers(HttpMethod.POST, BalanceController.RESOURCE_PATH + "/**")
            .hasAuthority(AuthorityUserPrivileges.AUTH_SELF_TRANSACTION_CREATE)
            .requestMatchers(HttpMethod.PATCH, BalanceController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_TRANSACTION_EDIT, AuthorityAdminPrivileges.AUTH_ADMIN_TRANSACTIONS_EDIT)
            // card controller endpoints
            .requestMatchers(HttpMethod.GET, CardsController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_CARD_VIEW, AuthorityAdminPrivileges.AUTH_ADMIN_CARDS_VIEW)
            .requestMatchers(HttpMethod.POST, CardsController.RESOURCE_PATH + "/**")
            .hasAuthority(AuthorityUserPrivileges.AUTH_SELF_CARD_APPLY)
            .requestMatchers(HttpMethod.PATCH, CardsController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_CARD_EDIT, AuthorityAdminPrivileges.AUTH_ADMIN_CARDS_EDIT)
            // loans controller
            .requestMatchers(HttpMethod.GET, LoansController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_LOAN_VIEW, AuthorityAdminPrivileges.AUTH_ADMIN_LOANS_VIEW)
            .requestMatchers(HttpMethod.POST, LoansController.RESOURCE_PATH + "/**")
            .hasAuthority(AuthorityUserPrivileges.AUTH_SELF_LOAN_APPLY)
            .requestMatchers(HttpMethod.PATCH, LoansController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_LOAN_EDIT, AuthorityAdminPrivileges.AUTH_ADMIN_LOANS_EDIT)
            // user controller
            .requestMatchers(HttpMethod.GET, UserController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_USER_VIEW, AuthorityAdminPrivileges.AUTH_ADMIN_USERS_VIEW)
            .requestMatchers(HttpMethod.PATCH, UserController.RESOURCE_PATH + "/**")
            .hasAnyAuthority(AuthorityUserPrivileges.AUTH_SELF_USER_EDIT, AuthorityAdminPrivileges.AUTH_ADMIN_USERS_EDIT)
            .requestMatchers(HttpMethod.DELETE, UserController.RESOURCE_PATH + "/**")
            .hasAuthority(AuthorityUserPrivileges.AUTH_SELF_USER_DELETE)
            // swagger ui
            .requestMatchers(HttpMethod.GET,
                "/v3/api-docs/**", // the json schema
                "/swagger-ui/**",
                "/swagger-ui.html") // redirects to /swagger-ui/index.html
            .hasAuthority(AuthorityUserPrivileges.AUTH_USER)
            // actuator
            .requestMatchers("/actuator/**").hasAuthority(AuthorityAdminPrivileges.AUTH_ADMIN)
            // unsecured endpoints
            .requestMatchers(
                NoticesController.RESOURCE_PATH + "/**",
                ContactController.RESOURCE_PATH + "/**",
                RegisterController.RESOURCE_PATH + "/**",
                Constants.INVALID_SESSION_URL,
                "/"
            ).permitAll()
    );
    http.formLogin(configurer -> configurer
        .defaultSuccessUrl(Constants.DEFAULT_LOGIN_REDIRECT_URL)
        // these example success and failure handlers are similar to default spring security behavior
        .successHandler(new CustomAuthenticationSuccessHandler())
        .failureHandler(new CustomAuthenticationFailureHandler())
    ).logout(c -> c.invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID"));
    // configuration specific to http basic
    http.httpBasic(httpBasicConfigurer -> httpBasicConfigurer
        .authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint(objectMapper(), environment, isProd))
    );
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
   * For development environment usage only.
   * Populates the database with test users defined in a json resource file.
   * @param userRepository the {@link SecurityUserRepository} for interacting with postgres db
   * @param noticeDetailsRepository the {@link NoticeDetailsRepository} for interacting with postgres db
   * @param contactMessageRepository the {@link ContactMessageRepository} for interacting with postgres db
   * @return StartupDatabasePopulator
   */
  @Bean
  @Profile({ SpringProfileConstants.POSTGRES, SpringProfileConstants.H2 })
  public StartupDatabasePopulator startupDatabasePopulator(
      final SecurityUserRepository userRepository,
      final NoticeDetailsRepository noticeDetailsRepository,
      final ContactMessageRepository contactMessageRepository,
      final SecurityAuthorityRepository authorityRepository,
      final SecurityGroupRepository securityGroupRepository,
      final SecurityGroupAuthorityRepository securityGroupAuthorityRepository,
      final ExampleDataManager exampleDataManager,
      final AccountRepository accountRepository,
      final CardRepository cardRepository,
      final LoanRepository loanRepository,
      final UserGenerator userGenerator,
      final ObjectMapper objectMapper,
      final PasswordEncoder passwordEncoder,
      @Value("${example-data.regenerate:false}") boolean regenerateData,
      @Value("${example-data.enabled:true}") boolean enabled,
      @Value("${example-data.populateSecurityGroups:true}") boolean populateSecurityGroups,
      @Value("${example-data.users.populate:true}") boolean populateUsers,
      @Value("${example-data.notices.populate:true}") boolean populateNoticeDetails,
      @Value("${example-data.messages.populate:true}") boolean populateContactDetails,
      @Value("${example-data.accounts.populate:true}") boolean populateAccounts,
      @Value("${example-data.cards.populate:true}") boolean populateCards,
      @Value("${example-data.loans.populate:true}") boolean populateLoans
  ) {
    return StartupDatabasePopulator.builder()
        .exampleDataManager(exampleDataManager)
        .securityUserRepository(userRepository)
        .noticeDetailsRepository(noticeDetailsRepository)
        .contactMessageRepository(contactMessageRepository)
        .accountRepository(accountRepository)
        .cardRepository(cardRepository)
        .loanRepository(loanRepository)
        .authorityRepository(authorityRepository)
        .securityGroupRepository(securityGroupRepository)
        .securityGroupAuthorityRepository(securityGroupAuthorityRepository)
        .userGenerator(userGenerator)
        .objectMapper(objectMapper)
        .passwordEncoder(passwordEncoder)
        .regenerateData(regenerateData)
        .enabled(enabled)
        .populateSecurityGroups(populateSecurityGroups)
        .populateUsers(populateUsers)
        .populateNoticeDetails(populateNoticeDetails)
        .populateContactDetails(populateContactDetails)
        .populateAccounts(populateAccounts)
        .populateCards(populateCards)
        .populateLoans(populateLoans)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
    final CardGenerator cardGenerator = new CardGenerator(faker(), objectMapper());
    cardGenerator.setItemCount(cardCount);
    return cardGenerator;
  }

  @Bean
  public LoanGenerator loanGenerator(@Value("${example-data.loans.count:20}") int loanCount) {
    LoanGenerator loanGenerator = new LoanGenerator(faker(), objectMapper());
    loanGenerator.setItemCount(loanCount);
    return loanGenerator;
  }

  @Bean
  public NoticeDetailsGenerator noticeDetailsFileGenerator(@Value("${example-data.notices.count:20}") int noticeCount) {
    final NoticeDetailsGenerator noticeDetailsGenerator = new NoticeDetailsGenerator(faker(), objectMapper());
    noticeDetailsGenerator.setItemCount(noticeCount);
    return noticeDetailsGenerator;
  }

  @Bean
  public ContactMessageGenerator contactMessagesFileGenerator(@Value("${example-data.messages.count:20}") int messageCount) {
    final ContactMessageGenerator contactMessageGenerator = new ContactMessageGenerator(faker(), objectMapper());
    contactMessageGenerator.setItemCount(messageCount);
    return contactMessageGenerator;
  }

  @Bean
  public AccountGenerator accountFileGenerator(@Value("${example-data.accounts.count:1}") int accountCount) {
    final AccountGenerator accountGenerator = new AccountGenerator(faker(), objectMapper());
    accountGenerator.setItemCount(accountCount);
    return accountGenerator;
  }

  @Bean
  public UserGenerator userFileGenerator(
      @Value("${example-data.users.count:20}") int userCount,
      @NotNull SecurityGroupRepository securityGroupRepository,
      @NotNull SecurityAuthorityRepository authorityRepository
  ) {
    final UserGenerator userGenerator = new UserGenerator(faker(), objectMapper())
        .setSecurityGroupRepository(securityGroupRepository)
        .setAuthorityRepository(authorityRepository);
    userGenerator.setItemCount(userCount);
    return userGenerator;
  }

  @Bean
  public ExampleDataGenerationService exampleDataGenerationService(
      UserGenerator userGenerator,
      AccountGenerator accountGenerator,
      LoanGenerator loanGenerator,
      CardGenerator cardGenerator,
      NoticeDetailsGenerator noticeDetailsGenerator,
      ContactMessageGenerator contactMessageGenerator
  ) {
    return ExampleDataGenerationService.builder()
        .userGenerator(userGenerator)
        .accountGenerator(accountGenerator)
        .loanGenerator(loanGenerator)
        .cardGenerator(cardGenerator)
        .noticeDetailsGenerator(noticeDetailsGenerator)
        .contactMessageGenerator(contactMessageGenerator)
        .build();
  }

  @Bean
  public AuthenticationEvents authenticationEventListeners(
      AuthenticationAttemptManager authenticationAttemptManager,
      SecurityUserService userService) {
    return AuthenticationEvents.builder()
        .authenticationAttemptManager(authenticationAttemptManager)
        .userService(userService)
        .build();
  }

  @Bean
  public AuthorizationEventPublisher authorizationEventPublisher
      (ApplicationEventPublisher applicationEventPublisher) {
    return new SpringAuthorizationEventPublisher(applicationEventPublisher);
  }

  @Bean
  public AuthorizationEvents authorizationEventListeners() {
    return new AuthorizationEvents();
  }

  @Bean
  public AuthenticationAttemptManager authenticationAttemptManager(AuthenticationAttemptRepository attemptRepository) {
    return new AuthenticationAttemptManager().setAttemptRepository(attemptRepository);
  }

  @Bean
  public UserDetailsManager userDetailsManager(
      AuthenticationManager authenticationManager,
      SecurityUserRepository securityUserRepository,
      PasswordEncoder passwordEncoder,
      UserCache userCache,
      SecurityUserValidationService securityUserValidationService
  ) {
    return UserDetailsManagerImpl.builder()
        .authenticationManager(authenticationManager)
        .userRepository(securityUserRepository)
        .passwordEncoder(passwordEncoder)
        .userValidationService(securityUserValidationService)
        .userCache(userCache)
        .build();
  }

  @Bean
  public SecurityUserService securityUserService(UserDetailsManager userDetailsManager) {
    return SecurityUserServiceImpl.builder()
        .userDetailsManager(userDetailsManager)
        .build();
  }

  @Bean
  public SecurityUserValidationService userValidationService(Validator validator) {
    return SecurityUserValidationServiceImpl.builder().validator(validator).build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public AccountService accountService(AccountRepository accountRepository, SecurityUserService securityUserService) {
    AccountServiceImpl accountService = AccountServiceImpl.builder()
        .accountRepository(accountRepository)
        .build();
    accountService.setSecurityUserService(securityUserService);
    return accountService;
  }

  @Bean
  public BalanceService balanceService(AccountTransactionRepository accountTransactionRepository, SecurityUserService securityUserService) {
    BalanceServiceImpl balanceService = BalanceServiceImpl.builder()
        .accountTransactionRepository(accountTransactionRepository)
        .build();
    balanceService.setSecurityUserService(securityUserService);
    return balanceService;
  }

  @Bean
  public CardService cardService(CardRepository cardRepository, SecurityUserService securityUserService) {
    CardServiceImpl cardService = CardServiceImpl.builder()
        .cardRepository(cardRepository)
        .build();
    cardService.setSecurityUserService(securityUserService);
    return cardService;
  }

  @Bean
  public LoanService loanService(LoanRepository loanRepository, SecurityUserService securityUserService) {
    LoanServiceImpl loanService = LoanServiceImpl.builder()
        .loanRepository(loanRepository)
        .build();
    loanService.setSecurityUserService(securityUserService);
    return loanService;
  }

  @Bean
  public UserAuthorityManager userAuthorityManager(
      SecurityAuthorityRepository authorityRepository,
      SecurityGroupRepository groupRepository,
      SecurityUserRepository userRepository,
      UserCache userCache) {
    return UserAuthorityManager.builder()
        .authorityRepository(authorityRepository)
        .groupRepository(groupRepository)
        .userRepository(userRepository)
        .userCache(userCache)
        .build();
  }

  @Bean
  public UserCache userCache() {
    return UserCache.builder()
        .cache(CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(5, TimeUnit.MINUTES).build())
        .build();
  }
}
