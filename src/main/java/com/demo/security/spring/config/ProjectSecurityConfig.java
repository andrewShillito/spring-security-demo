package com.demo.security.spring.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.demo.security.spring.controller.AccountController;
import com.demo.security.spring.controller.BalanceController;
import com.demo.security.spring.controller.CardsController;
import com.demo.security.spring.controller.ContactController;
import com.demo.security.spring.controller.LoansController;
import com.demo.security.spring.controller.UserController;
import com.demo.security.spring.controller.NoticesController;
import com.demo.security.spring.generation.AccountGenerator;
import com.demo.security.spring.generation.CardGenerator;
import com.demo.security.spring.generation.ContactMessagesFileGenerator;
import com.demo.security.spring.generation.LoanGenerator;
import com.demo.security.spring.generation.NoticeDetailsFileGenerator;
import com.demo.security.spring.generation.UserFileGenerator;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.service.ExampleDataGenerationService;
import com.demo.security.spring.service.InMemoryLoginService;
import com.demo.security.spring.service.JpaLoginService;
import com.demo.security.spring.service.LoginService;
import com.demo.security.spring.service.SpringDataJpaUserDetailsService;
import com.demo.security.spring.utils.DevEnvironmentDbPopulator;
import com.demo.security.spring.service.DevEnvironmentExampleDataManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectSecurityConfig {

  /**
   * A spring profile which uses an in memory user details service - disables docker-compose startup
   * of postgres and adminer
   */
  public static final String PROFILE_IN_MEMORY_USERS = "inMemoryUsers";

  /** A profile which uses postgres database */
  public static final String PROFILE_POSTGRES = "postgres";

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf().disable().authorizeHttpRequests((requests) -> requests
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
   *
   * @return an in memory user details manager which is only safe for local demo or sample
   * applications
   */
  @Bean(name = "userDetailsService")
  @Profile(PROFILE_IN_MEMORY_USERS)
  public UserDetailsService inMemoryUserDetailsManager(final DevEnvironmentExampleDataManager devEnvironmentExampleDataManager) {
    return new InMemoryUserDetailsManager(devEnvironmentExampleDataManager.getInMemoryUsers());
  }

  /**
   * Create a jdbc user details manager. Note that the docker-compose file and
   * spring-boot-docker-compose by default start a postgres and adminer container.
   *
   * @param repository - a spring data jpa repository
   * @return JdbcUserDetailsManager
   */
  @Bean(name = "userDetailsService")
  @Profile("! " + PROFILE_IN_MEMORY_USERS)
  public UserDetailsService jpaUserDetailsService(final SecurityUserRepository repository) {
    return SpringDataJpaUserDetailsService
        .builder()
        .securityUserRepository(repository)
        .build();
  }

  /**
   * For development environment usage only.
   * Populates the database with test users defined in a json resource file.
   * @param repository the {@link SecurityUserRepository} for interacting with postgres db
   * @return DevEnvironmentDbPopulator
   */
  @Bean
  @Profile("! " + PROFILE_IN_MEMORY_USERS + " && " + PROFILE_POSTGRES)
  public DevEnvironmentDbPopulator devEnvironmentDbPopulator(final SecurityUserRepository repository, final DevEnvironmentExampleDataManager devEnvironmentExampleDataManager) {
    return DevEnvironmentDbPopulator.builder()
        .exampleDataManager(devEnvironmentExampleDataManager)
        .securityUserRepository(repository)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean(name = "loginService")
  @Profile(PROFILE_IN_MEMORY_USERS)
  public LoginService inMemoryLoginService(UserDetailsService userDetailsService) {
    if (!(userDetailsService instanceof InMemoryUserDetailsManager)) {
      throw new RuntimeException("Provided userDetailsService was expected to be InMemoryUserDetailsManager but was "
          + userDetailsService.getClass().getName());
    }
    return InMemoryLoginService.builder().userDetailsService(userDetailsService).build();
  }

  @Bean(name = "loginService")
  @Profile("! " + PROFILE_IN_MEMORY_USERS)
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
  public DevEnvironmentExampleDataManager exampleUsersManager(
      final ExampleDataGenerationService generationService,
      @Value("${example-data.regenerate:false}") boolean regenerateData
  ) {
    return DevEnvironmentExampleDataManager.builder()
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
}
