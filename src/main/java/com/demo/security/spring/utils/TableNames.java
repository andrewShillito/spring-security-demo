package com.demo.security.spring.utils;

import java.util.Set;

/**
 * Known database table names
 */
public class TableNames {

  /** Database table name for security_users */
  public static final String USERS = "security_users";
  /** Database table name for authentication_attempts */
  public static final String AUTHENTICATION_ATTEMPTS = "authentication_attempts";
  /** Database table name for security_authorities */
  public static final String AUTHORITIES = "security_authorities";
  /** Database table name for accounts */
  public static final String ACCOUNTS = "accounts";
  /** Database table name for account_transactions */
  public static final String ACCOUNT_TRANSACTIONS = "account_transactions";
  /** Database table name for loans */
  public static final String LOANS = "loans";
  /** Database table name for cards */
  public static final String CARDS = "cards";
  /** Database table name for notice_details */
  public static final String NOTICES = "notice_details";
  /** Database table name for contact_messages */
  public static final String CONTACT_MESSAGES = "contact_messages";

  /** The set of known table names */
  public static final Set<String> NAMES = Set.of(
      USERS,
      AUTHENTICATION_ATTEMPTS,
      AUTHORITIES,
      ACCOUNTS,
      ACCOUNT_TRANSACTIONS,
      LOANS,
      CARDS,
      NOTICES,
      CONTACT_MESSAGES
  );

}
