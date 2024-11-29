package com.demo.security.spring.service;

import static org.junit.jupiter.api.Assertions.*;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserDetailsManagerImplTest {

  @Autowired
  private UserDetailsManager userDetailsManager;

  @Autowired
  private TestDataGenerator testDataGenerator;

  @Autowired
  private SecurityUserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

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

  public void expectAssertionError(SecurityUser user) {
    assertThrows(AssertionError.class, () -> userDetailsManager.createUser(user));
  }

  public void expectUsernameNotFoundException(String username) {
    assertThrows(UsernameNotFoundException.class, () -> userDetailsManager.loadUserByUsername(username));
  }

}