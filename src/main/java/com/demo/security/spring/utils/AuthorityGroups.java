package com.demo.security.spring.utils;

import static com.demo.security.spring.utils.AuthorityAdminRoles.*;
import static com.demo.security.spring.utils.AuthorityUserRoles.*;

import java.util.Set;

public class AuthorityGroups {

  /** A group of users who have basic privileges to view and edit their own user information */
  public static final String GROUP_USER = "GROUP_USER";

  /** The set of rules which is granted if you belong to the ADMIN_USER group */
  public static final Set<String> GROUP_USER_ROLES = Set.of(
      ROLE_USER,
      ROLE_SELF_USER_VIEW,
      ROLE_SELF_USER_EDIT,
      ROLE_SELF_USER_DELETE
  );

  /** Grants roles associated with viewing and limited editing of a user's own accounts, loans, cards, and account transactions */
  public static final String GROUP_ACCOUNT_HOLDER = "GROUP_ACCOUNT_HOLDER";

  /** The set of rules which is granted if you belong to the ADMIN_USER group */
  public static final Set<String> GROUP_ACCOUNT_HOLDER_ROLES = Set.of(
      ROLE_USER,
      ROLE_SELF_LOAN_VIEW,
      ROLE_SELF_LOAN_APPLY,
      ROLE_SELF_LOAN_EDIT,
      ROLE_SELF_CARD_VIEW,
      ROLE_SELF_CARD_APPLY,
      ROLE_SELF_CARD_EDIT,
      ROLE_SELF_ACCOUNT_VIEW,
      ROLE_SELF_ACCOUNT_APPLY,
      ROLE_SELF_ACCOUNT_EDIT,
      ROLE_SELF_TRANSACTION_VIEW,
      ROLE_SELF_TRANSACTION_CREATE,
      ROLE_SELF_TRANSACTION_EDIT
  );

  /** A group of user roles which allows administrating user accounts */
  public static final String GROUP_ADMIN_USERS = "GROUP_ADMIN_USERS";

  /** The set of rules which is granted if you belong to the ADMIN_USERS group */
  public static final Set<String> GROUP_ADMIN_USERS_ROLES = Set.of(
      ROLE_USER,
      ROLE_ADMIN_USERS_VIEW,
      ROLE_ADMIN_USERS_EDIT
  );

  /** A group of user roles which allows administrating user accounts */
  public static final String GROUP_ADMIN_ACCOUNTS = "GROUP_ADMIN_ACCOUNTS";

  /** The set of rules which is granted if you belong to the ADMIN_ACCOUNTS group */
  public static final Set<String> GROUP_ADMIN_ACCOUNTS_ROLES = Set.of(
      ROLE_USER,
      ROLE_ADMIN_ACCOUNTS_VIEW,
      ROLE_ADMIN_ACCOUNTS_EDIT
  );

  /** A group of user roles which allows administrating user cards */
  public static final String GROUP_ADMIN_CARDS = "GROUP_ADMIN_CARDS";

  /** The set of rules which is granted if you belong to the ADMIN_CARDS group */
  public static final Set<String> GROUP_ADMIN_CARDS_ROLES = Set.of(
      ROLE_USER,
      ROLE_ADMIN_CARDS_VIEW,
      ROLE_ADMIN_CARDS_EDIT
  );

  /** A group of user roles which allows administrating user loans */
  public static final String GROUP_ADMIN_LOANS = "GROUP_ADMIN_LOANS";

  /** The set of rules which is granted if you belong to the ADMIN_LOANS group */
  public static final Set<String> GROUP_ADMIN_LOANS_ROLES = Set.of(
      ROLE_USER,
      ROLE_ADMIN_LOANS_VIEW,
      ROLE_ADMIN_LOANS_EDIT
  );

  /** A group of user roles which allows administrating user account transactions */
  public static final String GROUP_ADMIN_TRANSACTIONS = "GROUP_ADMIN_TRANSACTIONS";

  /** The set of rules which is granted if you belong to the ADMIN_TRANSACTIONS group */
  public static final Set<String> GROUP_ADMIN_TRANSACTIONS_ROLES = Set.of(
      ROLE_USER,
      ROLE_ADMIN_TRANSACTIONS_VIEW,
      ROLE_ADMIN_TRANSACTIONS_EDIT
  );

  /** A group of user roles which allows administrating the entire application */
  public static final String GROUP_ADMIN_SYSTEM = "GROUP_ADMIN_SYSTEM";

  /** The set of rules which is granted if you belong to the ADMIN_SYSTEM group */
  public static final Set<String> GROUP_ADMIN_SYSTEM_ROLES = Set.of(
      ROLE_USER,
      ROLE_ADMIN_USERS_VIEW,
      ROLE_ADMIN_USERS_EDIT,
      ROLE_ADMIN_ACCOUNTS_VIEW,
      ROLE_ADMIN_ACCOUNTS_EDIT,
      ROLE_ADMIN_CARDS_VIEW,
      ROLE_ADMIN_CARDS_EDIT,
      ROLE_ADMIN_LOANS_VIEW,
      ROLE_ADMIN_LOANS_EDIT,
      ROLE_ADMIN_TRANSACTIONS_VIEW,
      ROLE_ADMIN_TRANSACTIONS_EDIT
  );

}