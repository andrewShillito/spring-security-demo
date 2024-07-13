package com.demo.security.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DemoAssertions {

  /** For fuzzy matching of datetime */
  private static final List<Integer> FUZZ_FACTOR_LIST = List.of(-1, 0, 1);

  public static void assertDateEquals(ZonedDateTime expected, ZonedDateTime actual) {
    assertEquals(expected.truncatedTo(ChronoUnit.SECONDS), actual.truncatedTo(ChronoUnit.SECONDS));
  }

  public static void assertDateIsNowIsh(ZonedDateTime actual) {
    // round the date to the nearest 2 minutes for test stability...
    assertNotNull(actual);
    final ZonedDateTime nowTruncated = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
    final ZonedDateTime actualTruncated = actual.truncatedTo(ChronoUnit.MINUTES);
    assertTrue(FUZZ_FACTOR_LIST.stream().anyMatch(factor -> nowTruncated.plusMinutes(factor).equals(actualTruncated)),
        "Expected datetime to be now-ish but was " + actual);
    assertEquals(nowTruncated.truncatedTo(ChronoUnit.DAYS), actualTruncated.truncatedTo(ChronoUnit.DAYS));
  }

}
