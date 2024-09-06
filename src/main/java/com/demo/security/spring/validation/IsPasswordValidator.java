package com.demo.security.spring.validation;

import com.demo.security.spring.utils.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A validation constraint for user passwords.
 * Requires that a password is:
 * - 8-32 characters
 * - does not contain whitespace at the start or end
 * - contains only ascii printable characters
 * and the password meets all the below:
 * - 1 lowercase letter
 * - 1 uppercase letter
 * - 1 number
 * - 1 special character
 */
public class IsPasswordValidator implements ConstraintValidator<IsValidPassword, String> {

  /**
   * Returns true if the given string value meets requirements for a password
   * @param value password string to validate
   * @param context context in which the constraint is evaluated
   * @return true if the password is valid
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return StringUtils.isNotBlank(value)
        && meetsLengthRequirement(value)
        && wasTrimmed(value)
        && meetsCharacterContentRequirements(value);
  }

  /**
   * Returns true if all characters in the given string are ascii printable
   * and meets all the following requirements:
   * - 1 lowercase letter
   * - 1 uppercase letter
   * - 1 number
   * - 1 special character
   * @param s the string to test
   * @return true if the password string s meets the requirements for a valid password
   */
  private boolean meetsCharacterContentRequirements(String s) {
    boolean containsLowercaseLetter = false;
    boolean containsUppercaseLetter = false;
    boolean containsNumber = false;
    boolean containsSpecialCharacter = false;
    for (char c : s.toCharArray()) {
      if (CharUtils.isAsciiPrintable(c)) {
        if (CharUtils.isAsciiAlphaUpper(c)) {
          containsUppercaseLetter = true;
        } else if (CharUtils.isAsciiAlphaLower(c)) {
          containsLowercaseLetter = true;
        } else if (CharUtils.isAsciiNumeric(c)) {
          containsNumber = true;
        } else {
          containsSpecialCharacter = true;
        }
      } else {
        return false;
      }
    }
    return containsLowercaseLetter
        && containsUppercaseLetter
        && containsNumber
        && containsSpecialCharacter;
  }

  /**
   * Returns true if string s is non-null and between 8 and 24 characters inclusive.
   * @param s the string to test
   * @return true if the string is not null and between 8 and 24 characters inclusive
   */
  private boolean meetsLengthRequirement(String s) {
    return s != null && s.length() >= Constants.PASSWORD_MIN_LENGTH && s.length() <= Constants.PASSWORD_MAX_LENGTH;
  }

  /**
   * Asserts that a password was correctly trimmed before being validated.
   * Prevents saving a password that starts or ends with whitespace.
   * @param s the string to test
   * @return true if the string s does not start or end with a whitespace character
   */
  private boolean wasTrimmed(String s) {
    return !StringUtils.isWhitespace(s.substring(0, 1)) && !StringUtils.isWhitespace(s.substring(s.length() - 1));
  }

}
