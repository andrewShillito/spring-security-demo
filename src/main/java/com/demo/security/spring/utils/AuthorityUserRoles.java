package com.demo.security.spring.utils;

public class AuthorityUserRoles {

  /** Placeholder basic role that someone is a user */
  public static final String ROLE_USER = "ROLE_USER";
  /** Role which allows a user to view their own loans */
  public static final String ROLE_SELF_LOAN_VIEW = "ROLE_SELF_LOAN_VIEW";
  /** Role which allows a user to create their own loans */
  public static final String ROLE_SELF_LOAN_APPLY = "ROLE_SELF_LOAN_APPLY";
  /** Role which allows a user to edit their own loans */
  public static final String ROLE_SELF_LOAN_EDIT = "ROLE_SELF_LOAN_EDIT";
  /** Role which allows a user to view their own loans */
  public static final String ROLE_SELF_CARD_VIEW = "ROLE_SELF_CARD_VIEW";
  /** Role which allows a user to create their own cards */
  public static final String ROLE_SELF_CARD_APPLY = "ROLE_SELF_CARD_APPLY";
  /** Role which allows a user to edit their own cards */
  public static final String ROLE_SELF_CARD_EDIT = "ROLE_SELF_CARD_EDIT";
  /** Role which allows a user to view their own cards */
  public static final String ROLE_SELF_ACCOUNT_VIEW = "ROLE_SELF_ACCOUNT_VIEW";
  /** Role which allows a user to create their own account */
  public static final String ROLE_SELF_ACCOUNT_APPLY = "ROLE_SELF_ACCOUNT_APPLY";
  /** Role which allows a user to edit their own account */
  public static final String ROLE_SELF_ACCOUNT_EDIT = "ROLE_SELF_ACCOUNT_EDIT";
  /** Role which allows a user to view their own account */
  public static final String ROLE_SELF_TRANSACTION_VIEW = "ROLE_SELF_TRANSACTION_VIEW";
  /** Role which allows a user to create their own account transactions */
  public static final String ROLE_SELF_TRANSACTION_CREATE = "ROLE_SELF_TRANSACTION_CREATE";
  /** Role which allows a user to edit their own account transactions */
  public static final String ROLE_SELF_TRANSACTION_EDIT = "ROLE_SELF_TRANSACTION_EDIT";
  /** Role which allows a user to view their own user account transactions */
  public static final String ROLE_SELF_USER_VIEW = "ROLE_SELF_USER_VIEW";
  /** Role which allows a user to edit their own user */
  public static final String ROLE_SELF_USER_EDIT = "ROLE_SELF_USER_EDIT";
  /** Role which allows a user to delete their own user */
  public static final String ROLE_SELF_USER_DELETE = "ROLE_SELF_USER_DELETE";

}
