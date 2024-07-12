package com.demo.security.spring.utils;

import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.model.UserType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UserFileGenerator extends AbstractFileGenerator {

  protected static final String DEFAULT_TESTING_PASSWORD = "password";
  protected static final int DEFAULT_RANDOM_USER_COUNT = 20;

  private int userCount = DEFAULT_RANDOM_USER_COUNT;

  private final AccountGenerator accountGenerator = new AccountGenerator();

  public UserFileGenerator(String fileName) {
    super(fileName);
  }

  public UserFileGenerator(String outputFileDir, String fileName) {
    super(outputFileDir, fileName);
  }

  public UserFileGenerator(String outputFileDir, String fileName, boolean overwriteFiles) {
    super(outputFileDir, fileName, overwriteFiles);
  }

  @Override
  public List<SecurityUser> generate() {
    log.info(() -> "Starting user generation");
    final List<SecurityUser> generatedUsers = generateUsers();
    log.info(() -> "Generated " + generatedUsers.size() + " users");
    return generatedUsers;
  }

  private List<SecurityUser> generateUsers() {
    final List<SecurityUser> users = new ArrayList<>(List.of(
        generateUser("user", false),
        generateUser("admin", true),
        generateUser("otherUser", false),
        generateUser("otherAdmin", true, List.of("ROLE_ADMIN", "ROLE_USER")),
        disable(generateUser("userDisabled", false)),
        disable(generateUser("adminDisabled", true))
    ));
    for (int i = 0; i < getItemCount(); i++) {
      final String username = faker.internet().username();
      users.add(faker.random().nextBoolean() ? generateRandomActiveUser(username) : generateRandomUser(username));
    }
    return users;
  }

  private SecurityUser generateRandomActiveUser(String username) {
    final String type = faker.random().nextBoolean() ? "external" : "internal";
    return generateUser(
        username,
        faker.internet().password(),
        type,
        getRolesForType(type),
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
        faker.internet().password(),
        type,
        getRolesForType(type),
    faker.random().nextBoolean(),
        faker.random().nextBoolean(),
        faker.random().nextBoolean(),
        faker.random().nextBoolean()
        );
  }

  private List<String> getRolesForType(String type) {
    if (type != null && type.equals("external")) {
      return List.of("ROLE_USER");
    }
    return List.of("ROLE_ADMIN");
  }

  private SecurityUser generateUser(String username, boolean isInternal) {
    return isInternal ? generateExternalUser(username, DEFAULT_TESTING_PASSWORD) : generateInternalUser(username, DEFAULT_TESTING_PASSWORD);
  }

  private SecurityUser generateUser(String username, boolean isInternal, List<String> roles) {
    final SecurityUser user = isInternal ? generateExternalUser(username, DEFAULT_TESTING_PASSWORD) : generateInternalUser(username, DEFAULT_TESTING_PASSWORD);
    user.setAuthorities(toAuthorities(roles));
    return user;
  }

  private SecurityUser generateExternalUser(String username, String password) {
    return generateUser(username, password, "external", getRolesForType("external"), true, false, false, false);
  }

  private SecurityUser generateInternalUser(String username, String password) {
    return generateUser(username, password, "internal", getRolesForType("internal"), true, false, false, false);
  }

  private SecurityUser generateUser(
      String username,
      String password,
      String type,
      List<String> roles,
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
    user.setUserType(UserType.valueOf(type));
    user.setUserRole(user.getUserType() == UserType.internal ? "ADMIN" : "STANDARD");
    user.setAuthorities(toAuthorities(roles));
    user.setEmail(username + "@demo.com");
    user.setAccounts(accountGenerator.generate());
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

  private List<SecurityAuthority> toAuthorities(List<String> roles) {
    return roles.stream().map(role -> {
        SecurityAuthority authority = new SecurityAuthority();
      authority.setAuthority(role);
      return authority;
    }).toList();
  }

  @Override
  public UserFileGenerator setItemCount(int itemCount) {
    return (UserFileGenerator) super.setItemCount(itemCount);
  }
}
