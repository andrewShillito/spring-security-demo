package com.demo.security.spring.service;

import com.demo.security.spring.generate.AccountGenerator;
import com.demo.security.spring.generate.CardGenerator;
import com.demo.security.spring.generate.ContactMessageGenerator;
import com.demo.security.spring.generate.LoanGenerator;
import com.demo.security.spring.generate.NoticeDetailsGenerator;
import com.demo.security.spring.generate.UserGenerator;
import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.ContactMessage;
import com.demo.security.spring.model.SecurityGroupConfig;
import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.NoticeDetails;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.AuthorityGroups;
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

  private UserGenerator userGenerator;

  private NoticeDetailsGenerator noticeDetailsGenerator;

  private ContactMessageGenerator contactMessageGenerator;

  private AccountGenerator accountGenerator;

  private CardGenerator cardGenerator;

  private LoanGenerator loanGenerator;

  /**
   * Note that this method does not write to json file as we need to persist first to have userIds.
   * This is not an issue with other model types.
   * @return a list of generated users with no id value set
   */
  public List<SecurityUser> generateUsers() {
    return userGenerator.generate();
  }

  /**
   * Generate minimal information required for mapping security groups to associated authorities
   * @return list of SecurityGroupConfig - never null
   */
  public List<SecurityGroupConfig> generateSecurityGroups() {
    List<SecurityGroupConfig> groupInfo = new ArrayList<>();
    groupInfo.add(
        new SecurityGroupConfig()
            .setGroupName(AuthorityGroups.GROUP_USER)
            .setAuthorities(AuthorityGroups.GROUP_USER_ROLES)
    );
    groupInfo.add(
        new SecurityGroupConfig()
            .setGroupName(AuthorityGroups.GROUP_ACCOUNT_HOLDER)
            .setAuthorities(AuthorityGroups.GROUP_ACCOUNT_HOLDER_ROLES)
    );
    groupInfo.add(
        new SecurityGroupConfig()
            .setGroupName(AuthorityGroups.GROUP_ADMIN_USERS)
            .setAuthorities(AuthorityGroups.GROUP_ADMIN_USERS_ROLES)
    );
    groupInfo.add(
        new SecurityGroupConfig()
            .setGroupName(AuthorityGroups.GROUP_ADMIN_ACCOUNTS)
            .setAuthorities(AuthorityGroups.GROUP_ADMIN_ACCOUNTS_ROLES)
    );
    groupInfo.add(
        new SecurityGroupConfig()
            .setGroupName(AuthorityGroups.GROUP_ADMIN_CARDS)
            .setAuthorities(AuthorityGroups.GROUP_ADMIN_CARDS_ROLES)
    );
    groupInfo.add(
        new SecurityGroupConfig()
            .setGroupName(AuthorityGroups.GROUP_ADMIN_LOANS)
            .setAuthorities(AuthorityGroups.GROUP_ADMIN_LOANS_ROLES)
    );
    groupInfo.add(
        new SecurityGroupConfig()
            .setGroupName(AuthorityGroups.GROUP_ADMIN_TRANSACTIONS)
            .setAuthorities(AuthorityGroups.GROUP_ADMIN_TRANSACTIONS_ROLES)
    );
    groupInfo.add(
        new SecurityGroupConfig()
            .setGroupName(AuthorityGroups.GROUP_ADMIN_SYSTEM)
            .setAuthorities(AuthorityGroups.GROUP_ADMIN_SYSTEM_ROLES)
    );
    return groupInfo;
  }

  public Account generateAccount(SecurityUser user) {
    Preconditions.checkNotNull(user);
    final Account account = accountGenerator.generateAccount();
    account.setUserId(user.getId());
    account.getAccountTransactions().forEach(accountTransaction -> accountTransaction.setUserId(user.getId()));
    return account;
  }

  public List<Account> generateAccounts(List<SecurityUser> users) {
    ValidationUtils.notEmpty(users, "Users to generate accounts for cannot be empty");
    final List<Account> accounts = new ArrayList<>();
    users.forEach(u -> accounts.add(generateAccount(u)));
    return accounts;
  }

  public List<Card> generateCards(SecurityUser user) {
    Preconditions.checkNotNull(user);
    List<Card> cards = new ArrayList<>();
    cardGenerator.generate().forEach(card -> {
      card.setUserId(user.getId());
      cards.add(card);
    });
    return cards;
  }

  public List<Card> generateCards(List<SecurityUser> users) {
    ValidationUtils.notEmpty(users);
    final List<Card> cards = new ArrayList<>(); // flat - not grouped by user. > 1 per user is expected and we don't need to differentiate here between users
    users.forEach(u -> cards.addAll(generateCards(u)));
    return cards;
  }

  public List<Loan> generateLoans(SecurityUser user) {
    Preconditions.checkNotNull(user);
    List<Loan> loans = new ArrayList<>();
    loanGenerator.generate().forEach(loan -> {
      loan.setUserId(user.getId());
      loans.add(loan);
    });
    return loans;
  }

  public List<Loan> generateLoans(List<SecurityUser> users) {
    ValidationUtils.notEmpty(users);
    final List<Loan> loans = new ArrayList<>(); // flat - not grouped by user. > 1 per user is expected and we don't need to differentiate here between users
    users.forEach(u -> loans.addAll(generateLoans(u)));
    return loans;
  }

  public List<NoticeDetails> generateNotices() {
    return noticeDetailsGenerator.generate();
  }

  public List<ContactMessage> generateMessages() {
    return contactMessageGenerator.generate();
  }
}
