package com.demo.security.spring.controller;

import com.demo.security.spring.controller.error.UserCreationException;
import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.service.LoginService;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

  public static final String RESOURCE_PATH = "/user";

  @Autowired
  private LoginService loginService;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Create a new user
   * @param user
   * @return
   */
  @PostMapping(RESOURCE_PATH)
  public ResponseEntity<String> registerUser(@Valid @RequestBody final SecurityUser user, BindingResult bindingResult) {
    ResponseEntity<String> responseEntity = null;
    if (bindingResult.hasErrors()) {
      throw new UserCreationException(HttpStatus.BAD_REQUEST, bindingResult.getAllErrors().toString());
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
      throw new UserCreationException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
    return responseEntity;
  }

  @Getter
  @Setter
  @Builder
  @JsonPropertyOrder(value = { "id", "username" })
  public static class UserCreationResponse {

    private Long id;

    private String username;

    private List<AuthorityCreationResponse> authorities = new ArrayList<>();

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

}
