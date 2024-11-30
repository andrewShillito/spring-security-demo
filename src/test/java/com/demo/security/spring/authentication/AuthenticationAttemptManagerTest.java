package com.demo.security.spring.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.model.AuthenticationAttempt;
import com.demo.security.spring.model.AuthenticationFailureReason;
import com.demo.security.spring.model.ClientInfo;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.AuthenticationAttemptRepository;
import com.demo.security.spring.service.UserCache;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
      assertEquals(user, userCache.get(user.getUsername()));
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

  @Test
  void testFailedAuthenticationAttempt() {
    final AuthenticationAttemptRepository mockRepository = mock(AuthenticationAttemptRepository.class);
    final AuthenticationAttemptManager attemptManager = new AuthenticationAttemptManager().setAttemptRepository(mockRepository);
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    final SecurityUser testUser = testDataGenerator.generateExternalUser(username, password, false);

    assertThrows(NullPointerException.class, () -> attemptManager.handleFailedAuthentication(null, null, null));
    attemptManager.handleFailedAuthentication(null, null, AuthenticationFailureReason.BAD_CREDENTIALS);
    attemptManager.handleFailedAuthentication(username, null, AuthenticationFailureReason.BAD_CREDENTIALS);
    attemptManager.handleFailedAuthentication(null, testUser, AuthenticationFailureReason.BAD_CREDENTIALS);
    attemptManager.handleFailedAuthentication(username, testUser, AuthenticationFailureReason.BAD_CREDENTIALS);

    ArgumentCaptor<AuthenticationAttempt> captor = ArgumentCaptor.forClass(AuthenticationAttempt.class);
    verify(mockRepository, times(4)).save(captor.capture());

    final List<AuthenticationAttempt> values = captor.getAllValues();
    assertEquals(4, values.size());

    validateAttempt(null, null, false,
        AuthenticationFailureReason.BAD_CREDENTIALS, values.getFirst());
    validateAttempt(username, null, false,
        AuthenticationFailureReason.BAD_CREDENTIALS, values.get(1));
    validateAttempt(username, testUser.getId(), false,
        AuthenticationFailureReason.BAD_CREDENTIALS, values.get(2));
    validateAttempt(username, testUser.getId(), false,
        AuthenticationFailureReason.BAD_CREDENTIALS, values.get(3));
  }

  @Test
  void testSuccessfulAuthenticationAttempt() {
    final AuthenticationAttemptRepository mockRepository = mock(AuthenticationAttemptRepository.class);
    final AuthenticationAttemptManager attemptManager = new AuthenticationAttemptManager().setAttemptRepository(mockRepository);
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    final SecurityUser testUser = testDataGenerator.generateExternalUser(username, password, false);

    attemptManager.handleSuccessfulAuthentication(null, null);
    attemptManager.handleSuccessfulAuthentication(username, null);
    attemptManager.handleSuccessfulAuthentication(null, testUser);
    attemptManager.handleSuccessfulAuthentication(username, testUser);

    ArgumentCaptor<AuthenticationAttempt> captor = ArgumentCaptor.forClass(AuthenticationAttempt.class);
    verify(mockRepository, times(3)).save(captor.capture());

    final List<AuthenticationAttempt> values = captor.getAllValues();
    assertEquals(3, values.size());

    validateAttempt(username, null, true, null, values.getFirst());
    validateAttempt(username, testUser.getId(), true, null, values.get(1));
    validateAttempt(username, testUser.getId(), true, null, values.get(2));
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

  private void validateAttempt(
      String expectedUsername,
      Long expectedUserId,
      boolean isSuccessful,
      AuthenticationFailureReason failureReason,
      AuthenticationAttempt toValidate) {
    assertEquals(expectedUsername, toValidate.getUsername());
    assertEquals(expectedUserId, toValidate.getUserId());
    assertEquals(isSuccessful, toValidate.isSuccessful());
    assertEquals(failureReason, toValidate.getFailureReason());
    DemoAssertions.assertDateIsNowIsh(toValidate.getAttemptTime());
    ClientInfo clientInfo = toValidate.getClientInfo();
    assertNotNull(clientInfo);
    assertEquals("127.0.0.1", clientInfo.getRemoteAddress());
    assertEquals("localhost", clientInfo.getRemoteHost());
  }

}
