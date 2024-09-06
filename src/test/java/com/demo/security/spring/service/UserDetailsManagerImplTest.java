package com.demo.security.spring.service;

import static org.junit.jupiter.api.Assertions.*;

import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.model.SecurityUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserDetailsManagerImplTest {

  @Autowired
  private UserDetailsManagerImpl userDetailsManager;

  @Autowired
  private TestDataGenerator testDataGenerator;

  @Test
  void testCreateUserValidPassword() {
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    final SecurityUser validUser = testDataGenerator.generateExternalUser(username, password, false);
    validUser.setPassword(password); // make password unencrypted
    userDetailsManager.createUser(validUser);
  }

  @Test
  void testCreateUserInValidPassword() {
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    SecurityUser invalidUser = testDataGenerator.generateExternalUser(username, password, false);
    // encrypted password fails validation due to being too long
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

  public void expectAssertionError(SecurityUser user) {
    assertThrows(AssertionError.class, () -> userDetailsManager.createUser(user));
  }

  public void expectUsernameNotFoundException(String username) {
    assertThrows(UsernameNotFoundException.class, () -> userDetailsManager.loadUserByUsername(username));
  }

}