package com.demo.security.spring;

import com.demo.security.spring.generate.AccountGenerator;
import com.demo.security.spring.generate.CardGenerator;
import com.demo.security.spring.generate.ContactMessageGenerator;
import com.demo.security.spring.generate.LoanGenerator;
import com.demo.security.spring.generate.NoticeDetailsGenerator;
import com.demo.security.spring.generate.UserGenerator;
import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.model.UserCreationRequest;
import com.demo.security.spring.repository.AccountRepository;
import com.demo.security.spring.repository.CardRepository;
import com.demo.security.spring.repository.LoanRepository;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.service.SecurityUserService;
import com.demo.security.spring.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
public class TestDataGenerator {

  @Autowired
  private Faker faker;

  private final Random random = new Random();

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserGenerator userGenerator;

  @Autowired
  private AccountGenerator accountGenerator;

  @Autowired
  private LoanGenerator loanGenerator;

  @Autowired
  private CardGenerator cardGenerator;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private LoanRepository loanRepository;

  @Autowired
  private CardRepository cardRepository;

  @Autowired
  private ContactMessageGenerator contactMessageGenerator;

  @Autowired
  private NoticeDetailsGenerator noticeDetailsGenerator;

  @Autowired
  private SecurityUserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public String randomUsername() {
    return faker.internet().username();
  }

  public String randomPassword() {
    String generatedPassword = faker.internet().password(Constants.PASSWORD_MIN_LENGTH, Constants.PASSWORD_MAX_LENGTH - 1,
        true, true, true);
    // there are times when faker password does not contain lower-case letter so we handle that case now for test stability
    int result = random.nextInt(0, 3) % 3;
    if (result == 0) {
      // lowercase at the start
      generatedPassword += randomLowerCaseLetter();
    } else if (result == 1) {
      // lowercase inside the string
      generatedPassword = generatedPassword.substring(0, 4) + randomLowerCaseLetter() + generatedPassword.substring(4);
    } else if (result == 2) {
      // lowercase at the end
      generatedPassword = generatedPassword + randomLowerCaseLetter();
    }
    return generatedPassword;
  }

  public char randomLowerCaseLetter() {
    // lowercase is ascii 97 to 122 inclusive
    return (char) random.nextInt(97, 123);
  }

  public List<SecurityUser> generateUsers(int count, boolean persist, boolean internal) {
    final List<SecurityUser> users = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      if (internal) {
        users.add(generateAdminUser(faker.internet().username(), randomPassword(), persist));
      } else {
        users.add(generateExternalUser(faker.internet().username(), randomPassword(), persist));
      }
    }
    return users;
  }

  public SecurityUser generateExternalUser(boolean persist) {
    final String username = randomUsername();
    final String userRawPassword = randomPassword();
    return generateExternalUser(username, userRawPassword, persist);
  }

  public SecurityUser generateExternalUser(@NonNull String username, @NonNull String password, boolean persist) {
    return generateExternalUser(username, password, persist, null);
  }

  public SecurityUser generateExternalUser(@NonNull String username, @NonNull String password, boolean persist, Consumer<SecurityUser> mutator) {
    final SecurityUser user = userGenerator.generateExternalUser(username, password);
    if (mutator != null) {
      mutator.accept(user);
    }
    if (persist) {
      userService.createUser(user);
    }
    return user;
  }

  public SecurityUser generateAdminUser(@NonNull String username, @NonNull String password, boolean persist) {
    return generateAdminUser(username, password, persist, null);
  }

  public SecurityUser generateAdminUser(@NonNull String username, @NonNull String password, boolean persist, Consumer<SecurityUser> mutator) {
    final SecurityUser user = userGenerator.generateAdminUser(username, password);
    if (mutator != null) {
      mutator.accept(user);
    }
    if (persist) {
      userService.createUser(user);
    }
    return user;
  }

  public List<Loan> generateLoans(SecurityUser user, int count) {
    final List<Loan> loans = loanGenerator.generate(count);
    loans.forEach(l -> l.setUserId(user.getId()));
    loanRepository.saveAll(loans);
    return loans;
  }

  public List<Account> generateAccounts(SecurityUser user, int count) {
    final List<Account> accounts = accountGenerator.generate(count);
    accounts.forEach(a -> a.setUserId(user.getId()));
    accountRepository.saveAll(accounts);
    return accounts;
  }

  public Account generateAccount(SecurityUser user) {
    Account account = accountGenerator.generateAccount();
    account.setUserId(user.getId());
    account.getAccountTransactions().forEach(txn -> txn.setUserId(user.getId()));
    accountRepository.save(account);
    return account;
  }

  public List<Card> generateCards(SecurityUser user, int count) {
    final List<Card> cards = cardGenerator.generate(count);
    cards.forEach(c -> c.setUserId(user.getId()));
    cardRepository.saveAll(cards);
    return cards;
  }

  public String randomEmail() {
    return faker.internet().emailAddress();
  }

  public UserCreationRequest randomUserCreationRequest() {
    return UserCreationRequest
        .builder()
        .username(randomUsername())
        .password(randomPassword())
        .email(randomEmail())
        .build();
  }
}
