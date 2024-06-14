package com.demo.security.spring.controller.error;

import com.google.common.base.Preconditions;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

public class BindingResultUtils {

  public static List<ErrorDetailsResponse> generateErrorDetails(@NotNull BindingResult result) {
    Preconditions.checkNotNull(result);
    final List<ErrorDetailsResponse> responses = new ArrayList<>();
    if (result.hasFieldErrors()) {
      result.getFieldErrors().forEach(it -> responses.add(mapFieldErrorToErrorDetail(it)));
    }
    return responses;
  }

  public static ErrorDetailsResponse mapFieldErrorToErrorDetail(@NotNull FieldError error) {
    Preconditions.checkNotNull(error);
    return ErrorDetailsResponse.builder()
        .fieldName(error.getField())
        .errorCode(error.getCode())
        .errorMessage(error.getDefaultMessage())
        .rejectedValue(error.getRejectedValue())
        .build();
  }

}
