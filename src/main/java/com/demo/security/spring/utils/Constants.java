package com.demo.security.spring.utils;

import java.math.RoundingMode;

public class Constants {

  /** Global rounding mode for BigDecimal types in the application */
  public static final RoundingMode GLOBAL_ROUNDING_MODE = RoundingMode.HALF_EVEN;

  /** Global scale for BigDecimal and other decimal types which may be rounded in the application */
  public static final int GLOBAL_SCALE = 2;

  /** For configuring and testing example CORS handling */
  public static final String[] EXAMPLE_ALLOWED_CORS_PATHS = new String[] { "http://localhost:9000", "https://localhost:9000" };

}
