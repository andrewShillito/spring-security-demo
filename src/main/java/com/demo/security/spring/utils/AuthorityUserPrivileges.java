package com.demo.security.spring.utils;

public class AuthorityUserPrivileges {

  /** Placeholder basic Privilege that someone is a user */
  public static final String AUTH_USER = "AUTH_USER";
  /** Privilege which allows a user to view their own loans */
  public static final String AUTH_SELF_LOAN_VIEW = "AUTH_SELF_LOAN_VIEW";
  /** Privilege which allows a user to create their own loans */
  public static final String AUTH_SELF_LOAN_APPLY = "AUTH_SELF_LOAN_APPLY";
  /** Privilege which allows a user to edit their own loans */
  public static final String AUTH_SELF_LOAN_EDIT = "AUTH_SELF_LOAN_EDIT";
  /** Privilege which allows a user to view their own loans */
  public static final String AUTH_SELF_CARD_VIEW = "AUTH_SELF_CARD_VIEW";
  /** Privilege which allows a user to create their own cards */
  public static final String AUTH_SELF_CARD_APPLY = "AUTH_SELF_CARD_APPLY";
  /** Privilege which allows a user to edit their own cards */
  public static final String AUTH_SELF_CARD_EDIT = "AUTH_SELF_CARD_EDIT";
  /** Privilege which allows a user to view their own cards */
  public static final String AUTH_SELF_ACCOUNT_VIEW = "AUTH_SELF_ACCOUNT_VIEW";
  /** Privilege which allows a user to create their own account */
  public static final String AUTH_SELF_ACCOUNT_APPLY = "AUTH_SELF_ACCOUNT_APPLY";
  /** Privilege which allows a user to edit their own account */
  public static final String AUTH_SELF_ACCOUNT_EDIT = "AUTH_SELF_ACCOUNT_EDIT";
  /** Privilege which allows a user to view their own account */
  public static final String AUTH_SELF_TRANSACTION_VIEW = "AUTH_SELF_TRANSACTION_VIEW";
  /** Privilege which allows a user to create their own account transactions */
  public static final String AUTH_SELF_TRANSACTION_CREATE = "AUTH_SELF_TRANSACTION_CREATE";
  /** Privilege which allows a user to edit their own account transactions */
  public static final String AUTH_SELF_TRANSACTION_EDIT = "AUTH_SELF_TRANSACTION_EDIT";
  /** Privilege which allows a user to view their own user account transactions */
  public static final String AUTH_SELF_USER_VIEW = "AUTH_SELF_USER_VIEW";
  /** Privilege which allows a user to edit their own user */
  public static final String AUTH_SELF_USER_EDIT = "AUTH_SELF_USER_EDIT";
  /** Privilege which allows a user to delete their own user */
  public static final String AUTH_SELF_USER_DELETE = "AUTH_SELF_USER_DELETE";

}
