package com.demo.security.spring.generate;

import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityGroup;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityAuthorityRepository;
import com.demo.security.spring.repository.SecurityGroupRepository;
import com.demo.security.spring.utils.AuthorityGroups;
import com.demo.security.spring.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;

@Log4j2
public class UserGenerator extends AbstractGenerator<List<SecurityUser>> {

  public static final String EXAMPLE_USERNAME_USER = "user";

  public static final String EXAMPLE_USERNAME_USER_OTHER = "otherUser";

  public static final String EXAMPLE_USERNAME_SYSTEM_ADMIN = "systemAdmin";

  public static final String EXAMPLE_USERNAME_SYSTEM_ADMIN_OTHER = "otherSystemAdmin";

  public static final String EXAMPLE_USERNAME_LOAN_ADMIN = "loanAdmin";

  public static final String EXAMPLE_USERNAME_CARD_ADMIN = "cardAdmin";

  public static final String EXAMPLE_USERNAME_ACCOUNT_ADMIN = "accountAdmin";

  public static final String EXAMPLE_USERNAME_USER_ADMIN = "userAdmin";

  public static final String EXAMPLE_USERNAME_TRANSACTION_ADMIN = "transactionAdmin";

  public static final String EXAMPLE_USERNAME_DISABLED_USER = "userDisabled";

  public static final String EXAMPLE_USERNAME_DISABLED_ADMIN = "transactionAdmin";

  public static final String DEFAULT_TESTING_PASSWORD = "password";

  private SecurityGroupRepository securityGroupRepository;

  private SecurityAuthorityRepository authorityRepository;

  private final Random random = new Random();

  public UserGenerator(Faker faker, ObjectMapper objectMapper) {
    this(faker, objectMapper, DEFAULT_ITEM_COUNT);
  }

  public UserGenerator(Faker faker,
      ObjectMapper objectMapper, int itemCount) {
    super(faker, objectMapper, itemCount);
  }

  public UserGenerator setSecurityGroupRepository(
      SecurityGroupRepository securityGroupRepository) {
    this.securityGroupRepository = securityGroupRepository;
    return this;
  }

  public UserGenerator setAuthorityRepository(
      SecurityAuthorityRepository authorityRepository) {
    this.authorityRepository = authorityRepository;
    return this;
  }

  @Override
  public List<SecurityUser> generate() {
    return generateUsers(getItemCount());
  }

  @Override
  public List<SecurityUser> generate(int count) {
    log.info(() -> "Starting user generation");
    final List<SecurityUser> generatedUsers = generateUsers(count);
    log.info(() -> "Generated " + generatedUsers.size() + " users");
    return generatedUsers;
  }

  private List<SecurityUser> generateUsers(int count) {
    final List<SecurityUser> users = new ArrayList<>(List.of(
        generateUser(EXAMPLE_USERNAME_USER, false),
        generateUser(EXAMPLE_USERNAME_SYSTEM_ADMIN, true),
        generateUser(EXAMPLE_USERNAME_USER_OTHER, false),
        generateUser(EXAMPLE_USERNAME_SYSTEM_ADMIN_OTHER, true),
        generateUser(EXAMPLE_USERNAME_LOAN_ADMIN, true, null, List.of(AuthorityGroups.GROUP_USER, AuthorityGroups.GROUP_ADMIN_LOANS)),
        generateUser(EXAMPLE_USERNAME_CARD_ADMIN, true, null, List.of(AuthorityGroups.GROUP_USER, AuthorityGroups.GROUP_ADMIN_CARDS)),
        generateUser(EXAMPLE_USERNAME_ACCOUNT_ADMIN, true, null, List.of(AuthorityGroups.GROUP_USER, AuthorityGroups.GROUP_ADMIN_ACCOUNTS)),
        generateUser(EXAMPLE_USERNAME_USER_ADMIN, true, null, List.of(AuthorityGroups.GROUP_USER, AuthorityGroups.GROUP_ADMIN_USERS)),
        generateUser(EXAMPLE_USERNAME_TRANSACTION_ADMIN, true, null, List.of(AuthorityGroups.GROUP_USER, AuthorityGroups.GROUP_ADMIN_TRANSACTIONS)),
        disable(generateUser( EXAMPLE_USERNAME_DISABLED_USER, false)),
        disable(generateUser(EXAMPLE_USERNAME_DISABLED_ADMIN, true))
    ));
    for (int i = 0; i < count; i++) {
      final String username = faker.internet().username();
      users.add(faker.random().nextBoolean() ? generateRandomActiveUser(username) : generateRandomUser(username));
    }
    return users;
  }

  private SecurityUser generateRandomActiveUser(String username) {
    final String type = faker.random().nextBoolean() ? "external" : "internal";
    return generateUser(
        username,
        generatePassword(),
        null,
        getGroupsForType(type),
        true,
        false,
        false,
        false
        );
  }

  private SecurityUser generateRandomUser(String username) {
    final String type = faker.random().nextBoolean() ? "external" : "internal";
    return generateUser(
        username,
        generatePassword(),
        null,
        getGroupsForType(type),
        faker.random().nextBoolean(),
        faker.random().nextBoolean(),
        faker.random().nextBoolean(),
        faker.random().nextBoolean()
        );
  }

  private Set<String> getGroupsForType(String type) {
    Set<String> groups = new HashSet<>();
    if (type != null && type.equals("external")) {
      groups.add(AuthorityGroups.GROUP_ACCOUNT_HOLDER);
      groups.add(AuthorityGroups.GROUP_USER);
    } else {
      groups.add(AuthorityGroups.GROUP_ADMIN_SYSTEM);
      groups.add(AuthorityGroups.GROUP_USER);
    }
    return groups;
  }

  private SecurityUser generateUser(String username, boolean isInternal) {
    return isInternal ? generateInternalUser(username, DEFAULT_TESTING_PASSWORD) :  generateExternalUser(username, DEFAULT_TESTING_PASSWORD);
  }

  private SecurityUser generateUser(String username, boolean isInternal, Collection<String> authorityNames, Collection<String> groups) {
    final SecurityUser user = isInternal ? generateInternalUser(username, DEFAULT_TESTING_PASSWORD) : generateExternalUser(username, DEFAULT_TESTING_PASSWORD);
    user.setGroups(getGroupsForNames(groups));
    user.setSecurityAuthorities(getAuthoritiesForNames(authorityNames));
    return user;
  }

  private Set<SecurityGroup> getGroupsForNames(Collection<String> groupNames) {
    if (groupNames != null && !groupNames.isEmpty()) {
      return securityGroupRepository.findAllByCodeIn(groupNames);
    }
    return new HashSet<>();
  }

  private Set<SecurityAuthority> getAuthoritiesForNames(Collection<String> authorityNames) {
    if (authorityNames != null && !authorityNames.isEmpty()) {
      return authorityRepository.findAllByAuthorityIn(authorityNames);
    }
    return new HashSet<>();
  }

  public SecurityUser generateExternalUser(String username, String password) {
    return generateUser(username, password, null, getGroupsForType("external"), true, false, false, false);
  }

  public SecurityUser generateInternalUser(String username, String password) {
    return generateUser(username, password, null, getGroupsForType("internal"), true, false, false, false);
  }

  private SecurityUser generateUser(
      String username,
      String password,
      Collection<String> authorities,
      Collection<String> groups,
      boolean enabled,
      boolean accountExpired,
      boolean passwordExpired,
      boolean isLocked
  ) {
    final SecurityUser user = new SecurityUser();
    user.setUsername(username);
    user.setPassword(password);
    user.setEnabled(enabled);
    user.setAccountExpired(accountExpired);
    user.setAccountExpiredDate(accountExpired ? randomPastDate() : null);
    user.setPasswordExpired(passwordExpired);
    user.setPasswordExpiredDate(passwordExpired ? randomPastDate() : null);
    user.setLocked(isLocked);
    user.setLockedDate(isLocked ? randomPastDate() : null);
    // user type and role will be replaced in the future with better role-based structures
    user.setGroups(getGroupsForNames(groups));
    user.setSecurityAuthorities(getAuthoritiesForNames(authorities));
    user.setEmail(username + "@demo.com");
    user.setControlDates(randomEntityControlDates());
    return user;
  }

  private SecurityUser expireAccount(final SecurityUser user) {
    user.setAccountExpired(true);
    user.setAccountExpiredDate(randomPastDate());
    return user;
  }

  private SecurityUser expirePassword(final SecurityUser user) {
    user.setPasswordExpired(true);
    user.setPasswordExpiredDate(randomPastDate());
    return user;
  }

  private SecurityUser disable(final SecurityUser user) {
    user.setEnabled(false);
    return user;
  }

  private SecurityUser lock(final SecurityUser user) {
    user.setLocked(true);
    user.setLockedDate(randomPastDate());
    return user;
  }

  private String generatePassword() {
    String generatedPassword = faker.internet().password(Constants.PASSWORD_MIN_LENGTH, Constants.PASSWORD_MAX_LENGTH,
        true, true, true);
    if (StringUtils.isWhitespace(generatedPassword.substring(0, 1))
        || StringUtils.isWhitespace(generatedPassword.substring(generatedPassword.length() - 1))) {
      generatedPassword = generatedPassword.trim();
      if (generatedPassword.length() < Constants.PASSWORD_MIN_LENGTH) {
        final StringBuilder sb = new StringBuilder(generatedPassword);
        for (int i = 0; i < Constants.PASSWORD_MIN_LENGTH - generatedPassword.length(); i++) {
          sb.append(random.nextInt(33, 127)); // 32 is space
        }
        generatedPassword = sb.toString();
      }
    }
    return generatedPassword;
  }
}
