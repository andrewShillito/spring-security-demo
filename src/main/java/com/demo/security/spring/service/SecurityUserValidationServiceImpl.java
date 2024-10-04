package com.demo.security.spring.service;

import com.demo.security.spring.model.PasswordWrapper;
import com.demo.security.spring.model.SecurityUser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.util.Set;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Note that this implementation does limited validation on fields other than password.
 * More complete validation of a user occurs at time of save during pre-persist.
 */
@Log4j2
@Builder
public class SecurityUserValidationServiceImpl implements SecurityUserValidationService {

  private Validator validator;

  @Override
  public void validateUser(UserDetails user, boolean validatePassword) {
    if (user == null) {
      throw new AssertionError("User cannot be null");
    } else if (!(user instanceof SecurityUser)) {
      throw new IllegalArgumentException("User details type is unsupported. Expected " + SecurityUser.class.getName() + " but was " + user.getClass().getName());
    }
    if (validatePassword) {
      validateUserPassword(user);
    }
  }

  @Override
  public void validateUserPassword(UserDetails user) {
    final Set<ConstraintViolation<PasswordWrapper>> errors = validator.validateProperty(
        new PasswordWrapper().setPassword(user.getPassword()), "password", Default.class);
    if (errors != null && !errors.isEmpty()) {
      throw new AssertionError("User password failed validation");
    }
  }

}
