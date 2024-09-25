package com.demo.security.spring.controller;

import com.demo.security.spring.controller.error.DuplicateUserException;
import com.demo.security.spring.controller.error.UserCreationException;
import com.demo.security.spring.controller.error.ValidationErrorDetailsResponse;
import com.demo.security.spring.error.ValidationErrorUtils;
import com.demo.security.spring.model.AuthorityCreationResponse;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.model.UserCreationRequest;
import com.demo.security.spring.model.UserCreationResponse;
import com.demo.security.spring.service.LoginService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RegisterController.RESOURCE_PATH)
public class RegisterController {

  public static final String RESOURCE_PATH = "/register";

  private LoginService loginService;

  private ObjectMapper objectMapper;

  @Autowired
  public void setLoginService(LoginService loginService) {
    this.loginService = loginService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Create a new user if no validation errors for the given user and the provided username is unique.
   * Otherwise, it throws {@link UserCreationException}.
   * @param userCreationRequest the user to be registered - must have a unique username
   * @param bindingResult the result of validating the provided user
   * @return a response entity containing {@link UserCreationResponse} object or throws {@link UserCreationException}
   */
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserCreationResponse> registerUser(
      @Valid @RequestBody final UserCreationRequest userCreationRequest,
      final BindingResult bindingResult)
      throws JsonProcessingException
  {
    ResponseEntity<UserCreationResponse> responseEntity = null;
    if (bindingResult.hasErrors()) {
      final String body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
          ValidationErrorUtils.generateErrorDetails(bindingResult));
      throw new UserCreationException(HttpStatus.BAD_REQUEST, body);
    }
    try {
      final SecurityUser createdUser = loginService.createUser(userCreationRequest.toSecurityUser());
      if (createdUser.getId() != null) {
        final UserCreationResponse response = UserCreationResponse
            .builder()
            .id(createdUser.getId())
            .username(createdUser.getUsername())
            .authorities(createdUser.getAuthorities().stream().map(AuthorityCreationResponse::fromAuthority).toList())
            .build();
        responseEntity = ResponseEntity
            .status(HttpStatus.CREATED.value())
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
      }
    } catch (DuplicateUserException e) {
      final ValidationErrorDetailsResponse response = ValidationErrorDetailsResponse.builder()
          .fieldName("username")
          .errorCode(DuplicateUserException.ERROR_CODE)
          .rejectedValue(userCreationRequest.getUsername())
          .errorMessage(e.getMessage())
          .build();
      throw new UserCreationException(HttpStatus.BAD_REQUEST,
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response), e);
    }
    return responseEntity;
  }

  @ExceptionHandler(value = { UserCreationException.class })
  public ResponseEntity<Object> handleUserCreationException(UserCreationException ex) {
    return ResponseEntity
        .status(ex.getStatusCode())
        .headers(ex.getHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .body(ex.getReason());
  }

}
