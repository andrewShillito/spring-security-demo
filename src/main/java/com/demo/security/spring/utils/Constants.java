package com.demo.security.spring.utils;

import java.math.RoundingMode;

public class Constants {

  /** Global rounding mode for BigDecimal types in the application */
  public static final RoundingMode GLOBAL_ROUNDING_MODE = RoundingMode.HALF_EVEN;

  /** Global scale for BigDecimal and other decimal types which may be rounded in the application */
  public static final int GLOBAL_SCALE = 2;

  /** For configuring and testing example CORS handling */
  public static final String[] EXAMPLE_ALLOWED_CORS_PATHS = new String[] { "http://localhost:9000", "https://localhost:9000" };

  /** Swagger uses this endpoint under the hood - required to be accessible in order for page at {@link #SWAGGER_UI_URL} to work */
  public static final String SWAGGER_UI_INTERNAL_URL = "/swagger-ui.html";
  /** Swagger UI url */
  public static final String SWAGGER_UI_URL = "/swagger-ui/index.html";
  /** Swagger schema url */
  public static final String SWAGGER_SCHEMA_URL = "/v3/api-docs";

  /** The default login redirect url */
  public static final String DEFAULT_LOGIN_REDIRECT_URL = SWAGGER_UI_URL;

  /** The example invalid session url */
  public static final String INVALID_SESSION_URL = "/invalidSession";

  /** The minimum allowed password length */
  public static final int PASSWORD_MIN_LENGTH = 8;

  /** The maximum allowed password length */
  public static final int PASSWORD_MAX_LENGTH = 32;

}
