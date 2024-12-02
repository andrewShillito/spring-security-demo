package com.demo.security.spring.service;

import static org.junit.jupiter.api.Assertions.*;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class UserDetailsManagerImplTest {

  @Autowired
  private UserDetailsManager userDetailsManager;

  @Autowired
  private TestDataGenerator testDataGenerator;

  @Autowired
  private SecurityUserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testCreateUserValidPassword() {
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    final SecurityUser validUser = testDataGenerator.generateExternalUser(username, password, false);
    validUser.setPassword(password); // make password unencrypted
    userDetailsManager.createUser(validUser);
    SecurityUser createdUser = (SecurityUser) userDetailsManager.loadUserByUsername(validUser.getUsername());
    assertNotNull(createdUser);
    DemoAssertions.assertIsBCryptHashed(createdUser.getPassword());
    DemoAssertions.assertUsersEqual(validUser, createdUser);
    SecurityUser fromDb = userRepository.getSecurityUserByUsername(validUser.getUsername());
    DemoAssertions.assertIsBCryptHashed(fromDb.getPassword());
    DemoAssertions.assertUsersEqual(validUser, fromDb);
  }

  @Test
  void testCreateUserInValidPassword() {
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    SecurityUser invalidUser = testDataGenerator.generateExternalUser(username, password, false);
    // encrypted password fails validation due to being too long
    invalidUser.setPassword(passwordEncoder.encode(password));
    expectAssertionError(invalidUser);

    invalidUser = new SecurityUser(invalidUser);
    invalidUser.setUsername(testDataGenerator.randomUsername());
    invalidUser.setPassword("A1a$bHt@"); // valid
    userDetailsManager.createUser(invalidUser);
    assertNotNull(userDetailsManager.loadUserByUsername(invalidUser.getUsername()));

    invalidUser = new SecurityUser(invalidUser);
    invalidUser.setUsername(testDataGenerator.randomUsername());
    invalidUser.setPassword("A1a$bHt"); // too short
    expectAssertionError(invalidUser);
    expectUsernameNotFoundException(invalidUser.getUsername());

    invalidUser = new SecurityUser(invalidUser);
    invalidUser.setUsername(testDataGenerator.randomUsername());
    invalidUser.setPassword("a1a$bht@"); // no uppercase letter
    expectAssertionError(invalidUser);
    expectUsernameNotFoundException(invalidUser.getUsername());

    invalidUser = new SecurityUser(invalidUser);
    invalidUser.setUsername(testDataGenerator.randomUsername());
    invalidUser.setPassword("A1a5bhtA"); // no special character
    expectAssertionError(invalidUser);
    expectUsernameNotFoundException(invalidUser.getUsername());

    invalidUser = new SecurityUser(invalidUser);
    invalidUser.setUsername(testDataGenerator.randomUsername());
    invalidUser.setPassword("z1a5bhtz"); // no special character and no uppercase
    expectAssertionError(invalidUser);
    expectUsernameNotFoundException(invalidUser.getUsername());

    invalidUser = new SecurityUser(invalidUser);
    invalidUser.setUsername(testDataGenerator.randomUsername());
    invalidUser.setPassword("Aaaabht@"); // no number
    expectAssertionError(invalidUser);
    expectUsernameNotFoundException(invalidUser.getUsername());

    invalidUser = new SecurityUser(invalidUser);
    invalidUser.setUsername(testDataGenerator.randomUsername());
    invalidUser.setPassword("A1a$bHt@A1a$bHt@A1a$bHt_A1a$bHt_"); // valid - max length
    userDetailsManager.createUser(invalidUser);
    assertNotNull(userDetailsManager.loadUserByUsername(invalidUser.getUsername()));

    invalidUser = new SecurityUser(invalidUser);
    invalidUser.setUsername(testDataGenerator.randomUsername());
    invalidUser.setPassword("A1a$bHt@A1a$bHt@A1a$bHt_A1a$bHt_@"); // invalid - too long
    expectAssertionError(invalidUser);
    expectUsernameNotFoundException(invalidUser.getUsername());
  }

  @Test
  void testUpdateUserEmail() {
    // create and assert user exists as expected
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    final SecurityUser validUser = testDataGenerator.generateExternalUser(username, password,
        false);
    validUser.setPassword(password); // make password unencrypted
    userDetailsManager.createUser(validUser);
    SecurityUser createdUser = (SecurityUser) userDetailsManager.loadUserByUsername(
        validUser.getUsername());
    assertNotNull(createdUser);
    DemoAssertions.assertUsersEqual(validUser, createdUser);

    final String previousEmail = validUser.getEmail();

    // update the user email
    createdUser.setEmail(testDataGenerator.randomEmail());
    userDetailsManager.updateUser(createdUser);

    SecurityUser updatedUser = (SecurityUser) userDetailsManager.loadUserByUsername(
        validUser.getUsername());
    assertNotNull(updatedUser);
    DemoAssertions.assertUsersEqual(validUser, updatedUser);

    assertNotEquals(previousEmail, updatedUser.getEmail());
  }

  @Test
  void testDeleteUser() {
    // create and assert user exists as expected
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    final SecurityUser validUser = testDataGenerator.generateExternalUser(username, password, false);
    validUser.setPassword(password); // make password unencrypted
    userDetailsManager.createUser(validUser);
    SecurityUser createdUser = (SecurityUser) userDetailsManager.loadUserByUsername(validUser.getUsername());
    assertNotNull(createdUser);
    DemoAssertions.assertUsersEqual(validUser, createdUser);

    // delete the user
    userDetailsManager.deleteUser(validUser.getUsername());
    expectUsernameNotFoundException(validUser.getUsername());
    assertFalse(userRepository.existsByUsernameIgnoreCase(validUser.getUsername()));
    assertNull(userRepository.getSecurityUserByUsername(validUser.getUsername()));
  }

  @Test
  void testChangePassword() throws Exception {
    String username = testDataGenerator.randomUsername();
    String password = testDataGenerator.randomPassword();
    SecurityUser user = testDataGenerator.generateExternalUser(username, password, true);

    // assert can log in using the existing password
    DemoAssertions.assertFormLoginSuccessful(mockMvc, username, password);

    // manually set the auth in the security context
    UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(user,
        password, ((SecurityUser) user).getAuthorities());
    authentication.setDetails(user);

    SecurityContextHolderStrategy contextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    SecurityContext context = contextHolderStrategy.getContext();
    context.setAuthentication(authentication);

    String newPassword = testDataGenerator.randomPassword();

    // invalid existing password
    assertThrows(RuntimeException.class, () -> userDetailsManager.changePassword(
        testDataGenerator.randomPassword(), newPassword));

    userDetailsManager.changePassword(password, newPassword);

    DemoAssertions.assertFormLogoutSuccessful(mockMvc);
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, username, password);
    DemoAssertions.assertFormLoginSuccessful(mockMvc, username, newPassword);
    DemoAssertions.assertFormLogoutSuccessful(mockMvc);

    // clean up
    SecurityContextHolder.getContextHolderStrategy().clearContext();

    // no user logged in
    assertThrows(RuntimeException.class, () -> userDetailsManager.changePassword(newPassword, testDataGenerator.randomPassword()));
  }

  @Test
  void testUserExists() {
    // invalid args
    assertFalse(userDetailsManager.userExists(null));
    assertFalse(userDetailsManager.userExists(""));
    assertFalse(userDetailsManager.userExists(" "));

    // valid args for unknown users
    assertFalse(userDetailsManager.userExists("asdfbasdfhjsafd"));
    assertFalse(userDetailsManager.userExists(testDataGenerator.randomUsername() + " should not exist"));

    String username = testDataGenerator.randomUsername();
    String password = testDataGenerator.randomPassword();
    testDataGenerator.generateExternalUser(username, password, true);
    assertTrue(userDetailsManager.userExists(username));
    assertTrue(userDetailsManager.userExists(username.toLowerCase()));
    assertTrue(userDetailsManager.userExists(username.toUpperCase()));
    assertFalse(userDetailsManager.userExists(username + " should not exist "));

    username = testDataGenerator.randomUsername();
    password = testDataGenerator.randomPassword();
    testDataGenerator.generateAdminUser(username, password, true);
    assertTrue(userDetailsManager.userExists(username));
    assertTrue(userDetailsManager.userExists(username.toLowerCase()));
    assertTrue(userDetailsManager.userExists(username.toUpperCase()));
    assertFalse(userDetailsManager.userExists(username + " should not exist "));
  }

  public void expectAssertionError(SecurityUser user) {
    assertThrows(AssertionError.class, () -> userDetailsManager.createUser(user));
  }

  public void expectUsernameNotFoundException(String username) {
    assertThrows(UsernameNotFoundException.class, () -> userDetailsManager.loadUserByUsername(username));
  }

}