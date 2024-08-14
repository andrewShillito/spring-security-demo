package com.demo.security.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.demo.security.spring.model.Loan;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

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
    assertEquals(expected.truncatedTo(ChronoUnit.SECONDS), actual.truncatedTo(ChronoUnit.SECONDS).withZoneSameInstant(ZoneId.systemDefault()));
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

  public static void assertLoansAreEmpty(List<Loan> loans) {
    assertTrue(loans == null || loans.isEmpty(), "Expected loans to be empty but found " + loans);
  }

  public static void assertBlank(String toTest) {
    assertTrue(org.apache.commons.lang3.StringUtils.isBlank(toTest), "Expected string to be blank but found " + toTest);
  }

  public static void assertEmpty(String toTest) {
    assertTrue(org.apache.commons.lang3.StringUtils.isEmpty(toTest), "Expected string to be empty but found " + toTest);
  }

}
