package com.demo.security.spring.generate;

import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.model.UserType;
import com.demo.security.spring.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;
import org.apache.commons.lang3.StringUtils;

@Log4j2
public class UserGenerator extends AbstractGenerator<List<SecurityUser>> {

  protected static final String DEFAULT_TESTING_PASSWORD = "password";

  private final Random random = new Random();

  public UserGenerator(Faker faker, ObjectMapper objectMapper) {
    this(faker, objectMapper, DEFAULT_ITEM_COUNT);
  }

  public UserGenerator(Faker faker,
      ObjectMapper objectMapper, int itemCount) {
    super(faker, objectMapper, itemCount);
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
        generateUser("user", false),
        generateUser("admin", true),
        generateUser("otherUser", false),
        generateUser("otherAdmin", true, List.of("ROLE_ADMIN", "ROLE_USER")),
        disable(generateUser("userDisabled", false)),
        disable(generateUser("adminDisabled", true))
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
        generatePassword(),
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
    return isInternal ? generateInternalUser(username, DEFAULT_TESTING_PASSWORD) :  generateExternalUser(username, DEFAULT_TESTING_PASSWORD);
  }

  private SecurityUser generateUser(String username, boolean isInternal, List<String> roles) {
    final SecurityUser user = isInternal ? generateInternalUser(username, DEFAULT_TESTING_PASSWORD) : generateExternalUser(username, DEFAULT_TESTING_PASSWORD);
    user.setAuthorities(toAuthorities(roles));
    return user;
  }

  public SecurityUser generateExternalUser(String username, String password) {
    return generateUser(username, password, "external", getRolesForType("external"), true, false, false, false);
  }

  public SecurityUser generateInternalUser(String username, String password) {
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
    List<SecurityAuthority> authorities = new ArrayList<>();
    roles.stream().forEach(role -> {
      SecurityAuthority authority = new SecurityAuthority();
      authority.setAuthority(role);
      authorities.add(authority);
    });
    return authorities;
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
