package com.demo.security.spring.utils;

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
import java.util.List;
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

  @EventListener(ContextRefreshedEvent.class)
  public void seedDatabaseIfEmpty() {
    try {
      populateUsers();
      populateNoticeDetails();
      populateContactMessages();

      int pageNumber = 0;
      int accountsCount = 0;
      int cardsCount = 0;
      int loansCount = 0;
      Page<SecurityUser> usersPage;
      do {
        PageRequest pageRequest = PageRequest.of(pageNumber, 100);
        usersPage = securityUserRepository.findAll(pageRequest);

        // populate accounts, cards, and loans
        accountsCount += populateAccounts(usersPage);
        cardsCount += populateCards(usersPage);
        loansCount += populateLoans(usersPage);

        pageNumber++;
        pageRequest = PageRequest.of(pageNumber, 100);
        usersPage = securityUserRepository.findAll(pageRequest);
      } while (usersPage.hasNext());

      log.info("Finished populating " + accountsCount + " development environment accounts");
      log.info("Finished populating " + cardsCount + " development environment cards");
      log.info("Finished populating " + loansCount + " development environment loans");

    } catch (Exception e) {
      throw new RuntimeException("Failed to populate development environment with error!", e);
    }
  }

  private void populateUsers() {
    if (securityUserRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment users as the table already contains data");
    } else {
      log.info(() -> "Populating development environment security users");
      final List<SecurityUser> users = exampleDataManager.getUsers();
      securityUserRepository.saveAll(users);
    }
  }

  private void populateNoticeDetails() {
    if (noticeDetailsRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment notice details as the table already contains data");
    } else {
      log.info(() -> "Populating development environment notice details");
      final List<NoticeDetails> noticeDetails = exampleDataManager.getNoticeDetails();
      noticeDetailsRepository.saveAll(noticeDetails);
      log.info(() -> "Finished populating " + noticeDetails.size() + " development environment notice details");
    }
  }

  private void populateContactMessages() {
    if (contactMessageRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment contact messages as the table already contains data");
    } else {
      log.info(() -> "Populating development environment contact messages");
      final List<ContactMessage> contactMessages = exampleDataManager.getContactMessages();
      contactMessageRepository.saveAll(contactMessages);
      log.info(() -> "Finished populating " + contactMessages.size() + " development environment contact messages");
    }
  }

  private int populateAccounts(Page<SecurityUser> users) {
    if (accountRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment accounts as the table already contains data");
      return 0;
    } else {
      final List<Account> accounts = exampleDataManager.getAccountsForUsers(users.getContent());
      accountRepository.saveAll(accounts);
      return accounts.size();
    }
  }

  private int populateCards(Page<SecurityUser> users) {
    if (cardRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment cards as the table already contains data");
      return 0;
    } else {
      log.info(() -> "Populating development environment cards");
      final List<Card> cards = exampleDataManager.getCardsForUsers(users.getContent());
      cardRepository.saveAll(cards);
      return cards.size();
    }
  }

  private int populateLoans(Page<SecurityUser> users) {
    if (loanRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment loans as the table already contains data");
      return 0;
    } else {
      log.info(() -> "Populating development environment loans");
      final List<Loan> loans = exampleDataManager.getLoansForUsers(users.getContent());
      loanRepository.saveAll(loans);
      return loans.size();
    }
  }

}
