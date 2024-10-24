package com.demo.security.spring.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A validation constraint for user security roles.
 * Implements validation for the following role name requirements:
 * - must be non-blank
 * - must start with the prefix 'ROLE_' or 'AUTH_'
 * - must be all upper-case characters
 * - must not contain whitespace
 * - must only contain A-Z, 0-9, and _ characters
 */
public class IsAuthValidator implements ConstraintValidator<IsValidAuth, String> {

  public static final String ROLE_PREFIX = "ROLE";
  public static final String AUTH_PREFIX = "AUTH";
  public static final String ROLE_PREFIX_WITH_UNDERSCORE = ROLE_PREFIX + "_";
  public static final String AUTH_PREFIX_WITH_UNDERSCORE = AUTH_PREFIX + "_";

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return StringUtils.isNotBlank(value)
        && (value.startsWith(ROLE_PREFIX_WITH_UNDERSCORE) || value.startsWith(AUTH_PREFIX_WITH_UNDERSCORE))
        && isAllValidCharacters(value)
        && !StringUtils.containsWhitespace(value);
  }

  private boolean isAllValidCharacters(String s) {
    for (char c : s.toCharArray()) {
      if (!CharUtils.isAsciiAlphaUpper(c) && !CharUtils.isAsciiNumeric(c) && c != '_') {
        return false;
      }
    }
    return true;
  }

}
