package com.demo.security.spring.authentication;

import static org.junit.jupiter.api.Assertions.*;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.model.AuthenticationAttempt;
import com.demo.security.spring.model.AuthenticationFailureReason;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.AuthenticationAttemptRepository;
import com.demo.security.spring.service.UserCache;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthenticationAttemptManagerTest {

  @Autowired
  private AuthenticationAttemptManager authenticationAttemptManager;

  @Autowired
  private AuthenticationAttemptRepository authenticationAttemptRepository;

  @Autowired
  private TestDataGenerator testDataGenerator;

  @Autowired
  private UserCache userCache;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testBadCredentials() throws Exception {
    SecurityUser user = setup();
    validateSingleAttempt(user, AuthenticationFailureReason.BAD_CREDENTIALS);
    // since a login attempt failed, the failed login attempts should have been incremented
    assertEquals(1, user.getFailedLoginAttempts());
    assertEquals(0, user.getNumPreviousLockouts());
    assertNull(user.getLockedDate());

    for (int i = 2; i < 7; i++) {
      attemptLogin(user.getUsername());
      assertEquals(i, user.getFailedLoginAttempts());
      assertEquals(0, user.getNumPreviousLockouts());
      validateLastAttempt(user, AuthenticationFailureReason.BAD_CREDENTIALS);
      if (i == 6) {
        assertTrue(user.isLocked());
        DemoAssertions.assertDateIsNowIsh(user.getLockedDate());
      } else {
        assertNull(user.getLockedDate());
        assertFalse(user.isLocked());
      }
    }
    assertEquals(6, getAttemptsForUser(user.getUsername()).size());

  }

  @Test
  void testDisabled() throws Exception {
    SecurityUser user = setup(it -> it.setEnabled(false));
    validateSingleAttempt(user, AuthenticationFailureReason.DISABLED);
  }

  @Test
  void testLocked() throws Exception {
    final String password = testDataGenerator.randomPassword();
    SecurityUser user = setup(password, it -> it.setLocked(true), true);
    validateSingleAttempt(user, AuthenticationFailureReason.LOCKED);
  }

  @Test
  void testAccountExpired() throws Exception {
    final String password = testDataGenerator.randomPassword();
    SecurityUser user = setup(password, it -> it.setAccountExpired(true), true);
    validateSingleAttempt(user, AuthenticationFailureReason.ACCOUNT_EXPIRED);
  }

  @Test
  void testCredentialsExpired() throws Exception {
    final String password = testDataGenerator.randomPassword();
    SecurityUser user = setup(password, it -> it.setPasswordExpired(true), true);
    validateSingleAttempt(user, AuthenticationFailureReason.CREDENTIALS_EXPIRED);
  }

  private SecurityUser setup() throws Exception {
    return setup(null, null, false);
  }

  private SecurityUser setup(Consumer<SecurityUser> consumer) throws Exception {
    return setup(null, consumer, false);
  }

  private SecurityUser setup(String password, Consumer<SecurityUser> consumer, boolean useValidPassword) throws Exception {
    final String username = testDataGenerator.randomUsername();
    password = password != null ? password : testDataGenerator.randomPassword();
    SecurityUser user = null;
    if (consumer != null) {
      user = testDataGenerator.generateExternalUser(username, password, true, consumer);
    } else {
      user = testDataGenerator.generateExternalUser(username, password, true);
    }
    validatePreconditions(user);
    if (useValidPassword) {
      attemptLogin(user.getUsername(), password);
    } else {
      attemptLogin(user.getUsername());
    }
    return user;
  }

  private void validatePreconditions(SecurityUser user) {
    assertTrue(userCache.isPresent(user.getUsername()));
    assertEquals(user, userCache.get(user.getUsername()));
  }

  private void attemptLogin(String username) throws Exception {
    attemptLogin(username, "invalidPassword1234");
  }

  private void attemptLogin(String username, String password) throws Exception {
    DemoAssertions.assertFormLoginUnSuccessful(mockMvc, username, password);
  }

  private List<AuthenticationAttempt> getAttemptsForUser(String username) {
    return authenticationAttemptRepository.findAllByUsername(username);
  }

  private void validateSingleAttempt(SecurityUser user, AuthenticationFailureReason reason) {
    List<AuthenticationAttempt> attemptList = getAttemptsForUser(user.getUsername());
    assertEquals(1, attemptList.size());
    validateAuthenticationAttemptForUser(user, attemptList.getFirst(), reason);
  }

  private void validateLastAttempt(SecurityUser user, AuthenticationFailureReason reason) {
    List<AuthenticationAttempt> attemptList = getAttemptsForUser(user.getUsername());
    assertFalse(attemptList.isEmpty(),
        "Expected authentication attempts for user " + user.getUsername() + " to be non-empty");
    validateAuthenticationAttemptForUser(user, attemptList.getLast(), reason);
  }

  private void validateAuthenticationAttemptForUser(SecurityUser user, AuthenticationAttempt attempt, AuthenticationFailureReason expectedReason) {
    DemoAssertions.assertBothNullOrNeitherAre(user, attempt);
    if (user == null) {
      return;
    }
    assertEquals(user.getId(), attempt.getUserId());
    assertEquals(user.getUsername(), attempt.getUsername());
    DemoAssertions.assertDateIsNowIsh(attempt.getAttemptTime());
    assertFalse(attempt.isSuccessful());
    assertEquals(expectedReason, attempt.getFailureReason());
    assertEquals("/login", attempt.getRequestedResource());
    // not much in client info when testing this way but can validate what is there at least
    assertEquals("127.0.0.1", attempt.getClientInfo().getRemoteAddress());
    assertEquals("localhost", attempt.getClientInfo().getRemoteHost());
  }

}
