package com.demo.security.spring.utils;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.AccountTransaction;
import com.demo.security.spring.model.TransactionType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AccountGenerator extends AbstractGenerator<List<Account>> {

  protected static final BigDecimal DEFAULT_STARTING_BALANCE = BigDecimal.valueOf(500.00);

  public AccountGenerator() {
    setItemCount(1);
  }

  @Override
  public List<Account> generate() {
    final List<Account> accounts = new ArrayList<>();
    for (int i = 0; i < getItemCount(); i++) {
      accounts.add(generateAccount());
    }
    log.info(() -> "Generated " + accounts.size() + " accounts");
    return accounts;
  }

  private Account generateAccount() {
    final Account account = new Account();
    account.setAccountType(faker.random().nextBoolean() ? "Savings" : "Checking");
    account.setBranchAddress(faker.address().fullAddress());
    account.setCreatedDate(randomEntityCreatedDate());
    final int upperBoundInclusive = faker.random().nextInt(5, 29);
    final List<AccountTransaction> transactions = new ArrayList<>();
    BigDecimal previousClosingBalance = DEFAULT_STARTING_BALANCE;
    for (int i = 0; i < upperBoundInclusive; i++) {
      transactions.add(generateAccountTransaction(previousClosingBalance));
      previousClosingBalance = transactions.getLast().getClosingBalance();
    }
    account.setAccountTransactions(transactions);
    return account;
  }

  private AccountTransaction generateAccountTransaction(BigDecimal previousClosingBalance) {
    final AccountTransaction accountTransaction = new AccountTransaction();
    accountTransaction.setCreatedDate(randomEntityCreatedDate());
    accountTransaction.setTransactionDate(randomPastDate());
    accountTransaction.setTransactionSummary(faker.commerce().productName());
    // add other transaction types as needed
    accountTransaction.setTransactionType(faker.random().nextBoolean() ? TransactionType.Withdrawal : TransactionType.Deposit);
    accountTransaction.setTransactionAmount(BigDecimal.valueOf(Math.abs(faker.random().nextDouble())).setScale(2, RoundingMode.HALF_EVEN));
    if (accountTransaction.getTransactionType() == TransactionType.Withdrawal) {
      accountTransaction.setClosingBalance(previousClosingBalance.subtract(accountTransaction.getTransactionAmount()).setScale(2, RoundingMode.HALF_EVEN));
    } else {
      accountTransaction.setClosingBalance(previousClosingBalance.add(accountTransaction.getTransactionAmount()).setScale(2, RoundingMode.HALF_EVEN));
    }
    return accountTransaction;
  }

}