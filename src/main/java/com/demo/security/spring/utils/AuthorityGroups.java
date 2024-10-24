package com.demo.security.spring.utils;

import static com.demo.security.spring.utils.AuthorityAdminPrivileges.*;
import static com.demo.security.spring.utils.AuthorityUserPrivileges.*;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The sets in this class result in a hierarchical roles structure
 * GROUP_ADMIN_SYSTEM < GROUP_ADMIN_USERS < GROUP_ACCOUNT_HOLDER < GROUP_USER
 * GROUP_ADMIN_SYSTEM < GROUP_ADMIN_ACCOUNTS < GROUP_ACCOUNT_HOLDER < GROUP_USER
 * GROUP_ADMIN_SYSTEM < GROUP_ADMIN_CARDS < GROUP_ACCOUNT_HOLDER < GROUP_USER
 * GROUP_ADMIN_SYSTEM < GROUP_ADMIN_LOANS < GROUP_ACCOUNT_HOLDER < GROUP_USER
 * GROUP_ADMIN_SYSTEM < GROUP_ADMIN_TRANSACTIONS < GROUP_ACCOUNT_HOLDER < GROUP_USER
 */
public class AuthorityGroups {

  /** A group of users who have basic privileges to view and edit their own user information */
  public static final String GROUP_USER = "GROUP_USER";

  /** The set of authorities which is granted if you belong to the ADMIN_USER group */
  public static final Set<String> GROUP_USER_AUTHS = Set.of(
      AUTH_USER,
      AUTH_SELF_USER_VIEW,
      AUTH_SELF_USER_EDIT,
      AUTH_SELF_USER_DELETE
  );

  /** Grants roles associated with viewing and limited editing of a user's own accounts, loans, cards, and account transactions */
  public static final String GROUP_ACCOUNT_HOLDER = "GROUP_ACCOUNT_HOLDER";

  /** The set of authorities which is granted if you belong to the ADMIN_USER group */
  public static final Set<String> GROUP_ACCOUNT_HOLDER_AUTHS = addAll(Set.of(
      AUTH_SELF_LOAN_VIEW,
      AUTH_SELF_LOAN_APPLY,
      AUTH_SELF_LOAN_EDIT,
      AUTH_SELF_CARD_VIEW,
      AUTH_SELF_CARD_APPLY,
      AUTH_SELF_CARD_EDIT,
      AUTH_SELF_ACCOUNT_VIEW,
      AUTH_SELF_ACCOUNT_APPLY,
      AUTH_SELF_ACCOUNT_EDIT,
      AUTH_SELF_TRANSACTION_VIEW,
      AUTH_SELF_TRANSACTION_CREATE,
      AUTH_SELF_TRANSACTION_EDIT
  ), GROUP_USER_AUTHS);

  /** A group of user roles which allows administrating user accounts */
  public static final String GROUP_ADMIN_USERS = "GROUP_ADMIN_USERS";

  /** The set of authorities which is granted if you belong to the ADMIN_USERS group */
  public static final Set<String> GROUP_ADMIN_USERS_AUTHS = addAll(Set.of(
      AUTH_ADMIN,
      AUTH_ADMIN_USERS_VIEW,
      AUTH_ADMIN_USERS_EDIT
  ), GROUP_ACCOUNT_HOLDER_AUTHS);

  /** A group of user roles which allows administrating user accounts */
  public static final String GROUP_ADMIN_ACCOUNTS = "GROUP_ADMIN_ACCOUNTS";

  /** The set of authorities which is granted if you belong to the ADMIN_ACCOUNTS group */
  public static final Set<String> GROUP_ADMIN_ACCOUNTS_AUTHS = addAll(Set.of(
      AUTH_ADMIN,
      AUTH_ADMIN_ACCOUNTS_VIEW,
      AUTH_ADMIN_ACCOUNTS_EDIT
  ), GROUP_ACCOUNT_HOLDER_AUTHS);

  /** A group of user roles which allows administrating user cards */
  public static final String GROUP_ADMIN_CARDS = "GROUP_ADMIN_CARDS";

  /** The set of authorities which is granted if you belong to the ADMIN_CARDS group */
  public static final Set<String> GROUP_ADMIN_CARDS_AUTHS = addAll(Set.of(
      AUTH_ADMIN,
      AUTH_ADMIN_CARDS_VIEW,
      AUTH_ADMIN_CARDS_EDIT
  ), GROUP_ACCOUNT_HOLDER_AUTHS);

  /** A group of user roles which allows administrating user loans */
  public static final String GROUP_ADMIN_LOANS = "GROUP_ADMIN_LOANS";

  /** The set of authorities which is granted if you belong to the ADMIN_LOANS group */
  public static final Set<String> GROUP_ADMIN_LOANS_AUTHS = addAll(Set.of(
      AUTH_ADMIN,
      AUTH_ADMIN_LOANS_VIEW,
      AUTH_ADMIN_LOANS_EDIT
  ), GROUP_ACCOUNT_HOLDER_AUTHS);

  /** A group of user roles which allows administrating user account transactions */
  public static final String GROUP_ADMIN_TRANSACTIONS = "GROUP_ADMIN_TRANSACTIONS";

  /** The set of authorities which is granted if you belong to the ADMIN_TRANSACTIONS group */
  public static final Set<String> GROUP_ADMIN_TRANSACTIONS_AUTHS = addAll(Set.of(
      AUTH_ADMIN,
      AUTH_ADMIN_TRANSACTIONS_VIEW,
      AUTH_ADMIN_TRANSACTIONS_EDIT
  ), GROUP_ACCOUNT_HOLDER_AUTHS);

  /** A group of user roles which allows administrating the entire application */
  public static final String GROUP_ADMIN_SYSTEM = "GROUP_ADMIN_SYSTEM";

  /** The set of authorities which is granted if you belong to the ADMIN_SYSTEM group */
  public static final Set<String> GROUP_ADMIN_SYSTEM_AUTHS = addAll(
      new HashSet<>(), List.of(
          GROUP_ADMIN_USERS_AUTHS,
          GROUP_ADMIN_ACCOUNTS_AUTHS,
          GROUP_ADMIN_CARDS_AUTHS,
          GROUP_ADMIN_LOANS_AUTHS,
          GROUP_ADMIN_TRANSACTIONS_AUTHS
      ));

  static Set<String> addAll(Set<String> target, Set<String> toAdd) {
    if (target != null) {
      Set<String> temp = new HashSet<>(target);
      if (toAdd != null) {
        temp.addAll(toAdd);
      }
      target = ImmutableSet.copyOf(temp);
    }
    return target;
  }

  static Set<String> addAll(Set<String> target, List<Set<String>> toAdd) {
    if (target != null) {
      Set<String> temp = new HashSet<>(target);
      if (toAdd != null && !toAdd.isEmpty()) {
        for (Set<String> s : toAdd) {
          temp = addAll(temp, s);
        }
      }
      target = ImmutableSet.copyOf(temp);
    }
    return target;
  }

}
