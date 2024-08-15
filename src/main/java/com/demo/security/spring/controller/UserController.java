package com.demo.security.spring.controller;

import com.demo.security.spring.controller.error.BindingResultUtils;
import com.demo.security.spring.controller.error.ValidationErrorDetailsResponse;
import com.demo.security.spring.controller.error.UserCreationException;
import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.service.LoginService;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UserController.RESOURCE_PATH)
public class UserController {

  public static final String RESOURCE_PATH = "/user";

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
   * @param user the user to be registered - must have a unique username
   * @param bindingResult the result of validating the provided user
   * @return a response entity containing {@link UserCreationResponse} object or throws {@link UserCreationException}
   */
  @PostMapping
  public ResponseEntity<String> registerUser(@Valid @RequestBody final SecurityUser user, final BindingResult bindingResult)
      throws JsonProcessingException {
    ResponseEntity<String> responseEntity = null;
    if (bindingResult.hasErrors()) {
      final String body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(
          BindingResultUtils.generateErrorDetails(bindingResult));
      throw new UserCreationException(HttpStatus.BAD_REQUEST, body);
    }
    try {
      final SecurityUser createdUser = loginService.createUser(user);
      if (createdUser.getId() != null) {
        final UserCreationResponse response = UserCreationResponse
            .builder()
            .id(createdUser.getId())
            .username(createdUser.getUsername())
            .authorities(createdUser.getAuthorities().stream().map(AuthorityCreationResponse::fromAuthority).toList())
            .build();
        responseEntity = ResponseEntity
            .status(HttpStatus.CREATED.value())
            .body(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
      }
    } catch (Exception e) {
      final String errorMessage = "Failed to create user with error: " + e.getMessage();
      final ValidationErrorDetailsResponse response = ValidationErrorDetailsResponse.builder().errorMessage(errorMessage).build();
      throw new UserCreationException(HttpStatus.BAD_REQUEST,
          objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response), e);
    }
    return responseEntity;
  }

  @GetMapping
  public SecurityUser getUserDetails(Authentication authentication) {
    if (authentication != null) {
      return loginService.getUser(authentication);
    }
    return null;
  }

  @Getter
  @Setter
  @Builder
  @JsonPropertyOrder(value = { "id", "username" })
  public static class UserCreationResponse {

    private Long id;

    private String username;

    private List<AuthorityCreationResponse> authorities;

  }

  @Getter
  @Setter
  @Builder
  @JsonPropertyOrder(value = { "id", "role" })
  public static class AuthorityCreationResponse {

    private String role;

    private Long id;

    static AuthorityCreationResponse fromAuthority(GrantedAuthority authority) {
      if (authority instanceof SecurityAuthority securityAuthority) {
        return AuthorityCreationResponse.builder()
            .id(securityAuthority.getId())
            .role(securityAuthority.getAuthority())
            .build();
      } else {
        throw new IllegalArgumentException("Authority implementations other than SimpleGrantedAuthority are not supported.");
      }
    }
  }

  @ExceptionHandler(value = { UserCreationException.class })
  public ResponseEntity<Object> handleUserCreationException(UserCreationException ex) {
    return ResponseEntity
        .status(ex.getStatusCode())
        .headers(ex.getHeaders())
        .body(ex.getReason());
  }

}
