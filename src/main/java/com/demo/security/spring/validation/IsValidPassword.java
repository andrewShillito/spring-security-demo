package com.demo.security.spring.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.demo.security.spring.utils.Constants;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IsPasswordValidator.class)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface IsValidPassword {

  String message() default "Invalid password. Passwords must be " + Constants.PASSWORD_MIN_LENGTH + " to "
      + Constants.PASSWORD_MAX_LENGTH + " characters and contain at least one lowercase letter, one uppercase letter,"
      + " one number, and one special character. Only ASCII printable characters are allowed.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

}
