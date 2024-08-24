package com.demo.security.spring.service;

import com.demo.security.spring.generate.AccountFileGenerator;
import com.demo.security.spring.generate.CardFileGenerator;
import com.demo.security.spring.generate.ContactMessagesFileGenerator;
import com.demo.security.spring.generate.LoanFileGenerator;
import com.demo.security.spring.generate.NoticeDetailsFileGenerator;
import com.demo.security.spring.generate.UserFileGenerator;
import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.ContactMessage;
import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.NoticeDetails;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.ValidationUtils;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Builder
@ToString
@Getter
public class ExampleDataGenerationService {

  private UserFileGenerator userFileGenerator;

  private NoticeDetailsFileGenerator noticeDetailsFileGenerator;

  private ContactMessagesFileGenerator contactMessagesFileGenerator;

  private AccountFileGenerator accountFileGenerator;

  private CardFileGenerator cardFileGenerator;

  private LoanFileGenerator loanFileGenerator;

  public List<SecurityUser> generateUsers(boolean writeToFile) {
    final List<SecurityUser> generatedUsers = userFileGenerator.generate();
    if (writeToFile) {
      userFileGenerator.write(generatedUsers);
    }
    return generatedUsers;
  }

  public Account generateAccount(SecurityUser user) {
    Preconditions.checkNotNull(user);
    final Account account = accountFileGenerator.generateAccount();
    account.setUser(user);
    return account;
  }

  public List<Account> generateAccounts(List<SecurityUser> users, boolean writeToFile) {
    ValidationUtils.notEmpty(users, "Users to generate accounts for cannot be empty");
    final List<Account> accounts = new ArrayList<>();
    users.forEach(u -> accounts.add(generateAccount(u)));
    if (writeToFile) {
      accountFileGenerator.write(accounts);
    }
    return accounts;
  }

  public List<Card> generateCards(SecurityUser user) {
    Preconditions.checkNotNull(user);
    return cardFileGenerator.generate().stream().peek(c -> c.setUser(user)).toList();
  }

  public List<Card> generateCards(List<SecurityUser> users, boolean writeToFile) {
    ValidationUtils.notEmpty(users);
    final List<Card> cards = new ArrayList<>(); // flat - not grouped by user. > 1 per user is expected and we don't need to differentiate here between users
    users.forEach(u -> cards.addAll(generateCards(u)));
    if (writeToFile) {
      cardFileGenerator.write();
    }
    return cards;
  }

  public List<Loan> generateLoans(SecurityUser user) {
    Preconditions.checkNotNull(user);
    return loanFileGenerator.generate().stream().peek(l -> l.setUser(user)).toList();
  }

  public List<Loan> generateLoans(List<SecurityUser> users, boolean writeToFile) {
    ValidationUtils.notEmpty(users);
    final List<Loan> loans = new ArrayList<>(); // flat - not grouped by user. > 1 per user is expected and we don't need to differentiate here between users
    users.forEach(u -> loans.addAll(generateLoans(u)));
    if (writeToFile) {
      loanFileGenerator.write();
    }
    return loans;
  }

  public List<NoticeDetails> generateNotices(boolean writeToFile) {
    final List<NoticeDetails> noticeDetails = noticeDetailsFileGenerator.generate();
    if (writeToFile) {
      noticeDetailsFileGenerator.write(noticeDetails);
    }
    return noticeDetails;
  }

  public List<ContactMessage> generateMessages(boolean writeToFile) {
    final List<ContactMessage> contactMessages = contactMessagesFileGenerator.generate();
    if (writeToFile) {
      contactMessagesFileGenerator.write(contactMessages);
    }
    return contactMessages;
  }
}
