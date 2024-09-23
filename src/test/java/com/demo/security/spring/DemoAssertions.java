package com.demo.security.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.security.spring.controller.error.AuthErrorDetailsResponse;
import com.demo.security.spring.model.EntityControlDates;
import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.Constants;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Log4j2
public class DemoAssertions {

  /** For fuzzy matching of datetime */
  private static final List<Integer> FUZZ_FACTOR_LIST = List.of(-1, 0, 1);

  /**
   * Checks that two zonedDateTime objects are equal when truncated to {@link ChronoUnit#SECONDS}.
   * Prevents erroneous failures relating to milliseconds/nano-time.
   * @param expected the expected zonedDateTime
   * @param actual the actual zonedDateTime
   */
  public static void assertDateEquals(ZonedDateTime expected, ZonedDateTime actual) {
    assertBothNullOrNeitherAre(expected, actual);
    if (expected != null) {
      assertEquals(expected.truncatedTo(ChronoUnit.SECONDS), actual.truncatedTo(ChronoUnit.SECONDS).withZoneSameInstant(ZoneId.systemDefault()));
    }
  }

  /**
   * Checks that a date is equal to now +/- 1 minute. Fuzzy matching is for testing stability
   * while still asserting that the datetime is roughly 'now'. Does not do any zone id conversion.
   * @param actual the datetime to check against truncated {@link ZonedDateTime#now()}
   */
  public static void assertDateIsNowIsh(ZonedDateTime actual) {
    assertNotNull(actual);
    final ZonedDateTime nowTruncated = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    final ZonedDateTime actualTruncated = actual.truncatedTo(ChronoUnit.MINUTES).withZoneSameInstant(ZoneId.systemDefault());
    assertTrue(FUZZ_FACTOR_LIST.stream().anyMatch(factor -> nowTruncated.plusMinutes(factor).equals(actualTruncated)),
        "Expected datetime to be now-ish relative to " + ZonedDateTime.now() + " but was " + actual);
    assertEquals(nowTruncated.truncatedTo(ChronoUnit.DAYS), actualTruncated.truncatedTo(ChronoUnit.DAYS));
  }

  public static void assertNotBlank(String s) {
    assertTrue(StringUtils.isNotBlank(s), "Expected string '" + s + "' to not be blank");
  }

  public static void assertNotEmpty(String s) {
    assertTrue(StringUtils.isNotEmpty(s), "Expected string '" + s + "' to not be empty");
  }

  public static void assertStartsWith(String toTest, String expectedPrefix) {
    assertNotEmpty(toTest);
    assertNotEmpty(expectedPrefix);
    assertTrue(toTest.startsWith(expectedPrefix), "Expected " + toTest + " to start with " + expectedPrefix + " but did not");
  }

  public static void assertLoansAreEmpty(List<Loan> loans) {
    assertTrue(loans == null || loans.isEmpty(), "Expected loans to be empty but found " + loans);
  }

  public static void assertBlank(String toTest) {
    assertTrue(org.apache.commons.lang3.StringUtils.isBlank(toTest), "Expected string to be blank but found " + toTest);
  }

  public static void assertEmpty(String toTest) {
    assertTrue(org.apache.commons.lang3.StringUtils.isEmpty(toTest), "Expected string to be empty but found " + toTest);
  }

  public static void assertAuthErrorEquals(AuthErrorDetailsResponse expected, AuthErrorDetailsResponse actual) {
    assertEquals(expected.getErrorCode(), actual.getErrorCode());
    assertEquals(expected.getErrorMessage(), actual.getErrorMessage());
    assertEquals(expected.getRequestUri(), actual.getRequestUri());
    assertEquals(expected.getRealm(), actual.getRealm());
    assertEquals(expected.getAdditionalInfo(), actual.getAdditionalInfo());
    DemoAssertions.assertDateEquals(expected.getTime(), actual.getTime());
    assertEquals(ZoneId.of("UTC"), actual.getTime().getZone());
  }

  public static MvcResult assertFormLoginSuccessful(
      @NonNull MockMvc mockMvc,
      @NonNull String username,
      @NonNull String password
  ) throws Exception {
    return assertFormLoginSuccessful(mockMvc, username, password, false);
  }

  public static MvcResult assertFormLoginSuccessful(
      @NonNull MockMvc mockMvc,
      @NonNull String username,
      @NonNull String password,
      boolean isSecure
  ) throws Exception {
    if (isSecure) {
      return mockMvc.perform(post("/login")
              .secure(true)
              .accept(MediaType.TEXT_HTML)
              .param("username", username)
              .param("password", password)
              .with(csrf()))
          .andExpect(status().isFound())
          .andExpect(authenticated().withUsername(username))
          .andExpect(redirectedUrl(Constants.DEFAULT_LOGIN_REDIRECT_URL))
          .andReturn();
    } else {
      return mockMvc.perform(formLogin().user(username).password(password))
          .andExpect(status().isFound())
          .andExpect(authenticated().withUsername(username))
          .andExpect(redirectedUrl(Constants.DEFAULT_LOGIN_REDIRECT_URL))
          .andReturn();
    }
  }

  public static MvcResult assertFormLoginUnSuccessful(
      @NonNull MockMvc mockMvc,
      @NonNull String username,
      @NonNull String password
  ) throws Exception {
    return assertFormLoginUnSuccessful(mockMvc, username, password, false);
  }

  public static MvcResult assertFormLoginUnSuccessful(
      @NonNull MockMvc mockMvc,
      @NonNull String username,
      @NonNull String password,
      boolean isSecure
  ) throws Exception {
    if (isSecure) {
      return mockMvc.perform(post("/login")
              .secure(true)
              .accept(MediaType.TEXT_HTML)
              .param("username", username)
              .param("password", password)
              .with(csrf()))
          .andExpect(status().isFound())
          .andExpect(unauthenticated())
          .andExpect(redirectedUrl("/login?error"))
          .andReturn();
    } else {
      return mockMvc.perform(formLogin().user(username).password(password))
          .andExpect(status().isFound())
          .andExpect(unauthenticated())
          .andExpect(redirectedUrl("/login?error"))
          .andReturn();
    }
  }

  public static void assertFormLogoutSuccessful(@NonNull MockMvc mockMvc) throws Exception {
    assertFormLogoutSuccessful(mockMvc, false);
  }

  public static void assertFormLogoutSuccessful(@NonNull MockMvc mockMvc, boolean isSecure) throws Exception {
    if (isSecure) {
      // just a note, if you don't include the MediaType of 'text/html' the app won't redirect to login
      // and will get 204 no content status instead with a null redirectUrl
      mockMvc.perform(post("/logout").secure(true).with(csrf()).accept(MediaType.TEXT_HTML))
          .andExpect(status().isFound())
          .andExpect(unauthenticated())
          .andExpect(redirectedUrl("/login?logout"));
    } else {
      mockMvc.perform(logout())
          .andExpect(status().isFound())
          .andExpect(unauthenticated())
          .andExpect(redirectedUrl("/login?logout"));
    }
  }

  public static void assertIsBCryptHashed(String hashedString) {
    assertStartsWith(hashedString, "{bcrypt}");
  }

  public static void assertUsersEqual(SecurityUser expected, SecurityUser actual) {
    assertBothNullOrNeitherAre(expected, actual);
    if (expected != null) {
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getUsername(), actual.getUsername());
      assertEquals(expected.getEmail(), actual.getEmail());
      assertEquals(expected.getUserType(), actual.getUserType());
      assertEquals(expected.getUserRole(), actual.getUserRole());
      assertEquals(expected.isEnabled(), actual.isEnabled());
      assertEquals(expected.isAccountExpired(), actual.isAccountExpired());
      assertEquals(expected.isAccountNonExpired(), actual.isAccountNonExpired());
      assertDateEquals(expected.getAccountExpiredDate(), actual.getAccountExpiredDate());
      assertEquals(expected.isPasswordExpired(), actual.isPasswordExpired());
      assertDateEquals(expected.getPasswordExpiredDate(), actual.getPasswordExpiredDate());
      assertEquals(expected.getFailedLoginAttempts(), actual.getFailedLoginAttempts());
      assertEquals(expected.getNumPreviousLockouts(), actual.getNumPreviousLockouts());
      assertEquals(expected.isLocked(), actual.isLocked());
      assertDateEquals(expected.getLockedDate(), actual.getLockedDate());
      assertDateEquals(expected.getUnlockDate(), actual.getUnlockDate());
      assertAuthoritiesEquals(expected.getAuthorities(), actual.getAuthorities());
      assertControlDatesEquals(expected.getControlDates(), actual.getControlDates());
    }
  }

  public static void assertAuthoritiesEquals(Collection<? extends GrantedAuthority> expected,
      Collection<? extends GrantedAuthority> actual) {
    assertBothNullOrNeitherAre(expected, actual);
    if (expected != null) {
      assertEquals(expected.size(), actual.size());
      for (var auth : expected) {
        assertNotNull(expected, "Found unexpectedly null authority in " + expected);
        assertTrue(actual.contains(auth));
      }
    }
  }

  public static void assertControlDatesEquals(EntityControlDates expected, EntityControlDates actual) {
    assertBothNullOrNeitherAre(expected, actual);
    if (expected != null) {
      assertDateEquals(expected.getCreated(), actual.getCreated());
      assertDateEquals(expected.getLastUpdated(), actual.getLastUpdated());
    }
  }

  public static void assertBothNullOrNeitherAre(Object expected, Object actual) {
    assertTrue((expected == null && actual == null) || (expected != null && actual != null));
  }
}
