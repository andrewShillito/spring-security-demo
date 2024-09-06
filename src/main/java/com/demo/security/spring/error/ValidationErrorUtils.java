package com.demo.security.spring.error;

import com.demo.security.spring.controller.error.ValidationErrorDetailsResponse;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class ValidationErrorUtils {

  public static List<ValidationErrorDetailsResponse> generateErrorDetails(@NonNull BindingResult result) {
    final List<ValidationErrorDetailsResponse> responses = new ArrayList<>();
    if (result.hasFieldErrors()) {
      result.getFieldErrors().forEach(it -> responses.add(mapFieldErrorToErrorDetail(it)));
    }
    return responses;
  }

  public static ValidationErrorDetailsResponse mapFieldErrorToErrorDetail(@NonNull FieldError error) {
    Preconditions.checkNotNull(error);
    return ValidationErrorDetailsResponse.builder()
        .fieldName(error.getField())
        .errorCode(error.getCode())
        .errorMessage(error.getDefaultMessage())
        .rejectedValue(error.getRejectedValue())
        .build();
  }

}
