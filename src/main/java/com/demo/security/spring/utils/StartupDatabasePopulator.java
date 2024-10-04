package com.demo.security.spring.utils;

import com.demo.security.spring.generate.JsonFileWriter;
import com.demo.security.spring.generate.UserGenerator;
import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.ContactMessage;
import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.NoticeDetails;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.AccountRepository;
import com.demo.security.spring.repository.CardRepository;
import com.demo.security.spring.repository.ContactMessageRepository;
import com.demo.security.spring.repository.LoanRepository;
import com.demo.security.spring.repository.NoticeDetailsRepository;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.service.ExampleDataManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
@Log4j2
public class StartupDatabasePopulator {

  private ExampleDataManager exampleDataManager;

  private SecurityUserRepository securityUserRepository;

  private NoticeDetailsRepository noticeDetailsRepository;

  private ContactMessageRepository contactMessageRepository;

  private AccountRepository accountRepository;

  private CardRepository cardRepository;

  private LoanRepository loanRepository;

  private PasswordEncoder passwordEncoder;

  private UserGenerator userGenerator;

  private ObjectMapper objectMapper;

  /** Should the startup user, account, loans, cards, account transactions, etc... be regenerated before startup db population */
  private boolean regenerateData;

  /** Enables / disables startup database population */
  private boolean enabled;

  @EventListener(ContextRefreshedEvent.class)
  public void seedDatabaseIfEmpty() {
    if (enabled) {
      try {
        populateUsers();
        populateNoticeDetails();
        populateContactMessages();

        int pageNumber = 0;
        List<Account> accounts = new ArrayList<>();
        List<Card> cards = new ArrayList<>();
        List<Loan> loans = new ArrayList<>();
        Page<SecurityUser> usersPage;
        do {
          PageRequest pageRequest = PageRequest.of(pageNumber, 100);
          usersPage = securityUserRepository.findAll(pageRequest);

          // populate accounts, cards, and loans
          accounts.addAll(populateAccounts(usersPage));
          cards.addAll(populateCards(usersPage));
          loans.addAll(populateLoans(usersPage));

          pageNumber++;
          pageRequest = PageRequest.of(pageNumber, 100);
          usersPage = securityUserRepository.findAll(pageRequest);
        } while (usersPage.hasNext());

        if (regenerateData) {
          final String accountsOutputFile = JsonFileWriter.DEFAULT_OUTPUT_DIRECTORY + ExampleDataManager.ACCOUNTS_OUTPUT_FILE_NAME;
          new JsonFileWriter<>(objectMapper, accountsOutputFile, accounts).write();
          final String cardsOutputFile = JsonFileWriter.DEFAULT_OUTPUT_DIRECTORY + ExampleDataManager.CARDS_OUTPUT_FILE_NAME;
          new JsonFileWriter<>(objectMapper, cardsOutputFile, cards).write();
          final String loansOutputFile = JsonFileWriter.DEFAULT_OUTPUT_DIRECTORY + ExampleDataManager.LOANS_OUTPUT_FILE_NAME;
          new JsonFileWriter<>(objectMapper, loansOutputFile, loans).write();
        }

        log.info("Finished populating " + accounts.size() + " development environment accounts");
        log.info("Finished populating " + cards.size() + " development environment cards");
        log.info("Finished populating " + loans.size() + " development environment loans");

      } catch (Exception e) {
        throw new RuntimeException("Failed to populate development environment with error!", e);
      }
    } else {
      log.info(() -> "Not seeding startup data as property example-data:enabled is false");
    }
  }

  private void populateUsers() {
    if (securityUserRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment users as the table already contains data");
    } else {
      log.info(() -> "Populating development environment security users");
      List<SecurityUser> users = exampleDataManager.getUsers();

      // make a list of just the raw passwords for reference in example-users.json
      final List<String> exampleDataPasswords = users.stream().map(SecurityUser::getPassword).toList();

      users.forEach(u -> u.setPassword(passwordEncoder.encode(u.getPassword())));
      securityUserRepository.saveAll(users);

      final List<Map<String, Object>> outputMap = new ArrayList<>();
      for (int i = 0; i < users.size(); i++) {
        Map<String, Object> userMap = users.get(i).toMap();
        userMap.put("password", exampleDataPasswords.get(i));
        outputMap.add(userMap);
      }

      if (regenerateData) {
        final String outputFile = JsonFileWriter.DEFAULT_OUTPUT_DIRECTORY + ExampleDataManager.USERS_OUTPUT_FILE_NAME;
        new JsonFileWriter<>(objectMapper, outputFile, outputMap).write();
        log.info(() -> "Finished populating " + users.size() + " development environment users");
      }
    }
  }

  private void populateNoticeDetails() {
    if (noticeDetailsRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment notice details as the table already contains data");
    } else {
      log.info(() -> "Populating development environment notice details");
      final List<NoticeDetails> noticeDetails = exampleDataManager.getNoticeDetails();
      noticeDetailsRepository.saveAll(noticeDetails);
      final String outputFile = JsonFileWriter.DEFAULT_OUTPUT_DIRECTORY + ExampleDataManager.NOTICES_OUTPUT_FILE_NAME;
      if (regenerateData) {
        new JsonFileWriter<>(objectMapper, outputFile, noticeDetails).write();
        log.info(() -> "Finished populating " + noticeDetails.size() + " development environment notice details");
      }
    }
  }

  private void populateContactMessages() {
    if (contactMessageRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment contact messages as the table already contains data");
    } else {
      log.info(() -> "Populating development environment contact messages");
      final List<ContactMessage> contactMessages = exampleDataManager.getContactMessages();
      contactMessageRepository.saveAll(contactMessages);
      final String outputFile = JsonFileWriter.DEFAULT_OUTPUT_DIRECTORY + ExampleDataManager.CONTACT_MESSAGES_OUTPUT_FILE_NAME;
      if (regenerateData) {
        new JsonFileWriter<>(objectMapper, outputFile, contactMessages).write();
        log.info(() -> "Finished populating " + contactMessages.size() + " development environment contact messages");
      }
    }
  }

  private List<Account> populateAccounts(Page<SecurityUser> users) {
    if (accountRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment accounts as the table already contains data");
      return new ArrayList<>();
    } else {
      final List<Account> accounts = exampleDataManager.getAccountsForUsers(users.getContent());
      accountRepository.saveAll(accounts);
      return accounts;
    }
  }

  private List<Card> populateCards(Page<SecurityUser> users) {
    if (cardRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment cards as the table already contains data");
      return new ArrayList<>();
    } else {
      log.info(() -> "Populating development environment cards");
      final List<Card> cards = exampleDataManager.getCardsForUsers(users.getContent());
      cardRepository.saveAll(cards);
      return cards;
    }
  }

  private List<Loan> populateLoans(Page<SecurityUser> users) {
    if (loanRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment loans as the table already contains data");
      return new ArrayList<>();
    } else {
      log.info(() -> "Populating development environment loans");
      final List<Loan> loans = exampleDataManager.getLoansForUsers(users.getContent());
      loanRepository.saveAll(loans);
      return loans;
    }
  }

}
