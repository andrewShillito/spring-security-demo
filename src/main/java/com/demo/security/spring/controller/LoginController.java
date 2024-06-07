package com.demo.security.spring.controller;

import com.demo.security.spring.model.SecurityAuthority;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

  public static final String RESOURCE_PATH = "/user";

  // TODO: setup different login services for in-memory vs. using db repository
  @Autowired
  private SecurityUserRepository securityUserRepository;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * TODO: handle error responses
   * TODO: implement roles and privleges
   *
   * @param user
   * @return
   */
  @PostMapping(RESOURCE_PATH)
  public ResponseEntity<String> registerUser(@Validated @RequestBody final SecurityUser user) {
    ResponseEntity<String> responseEntity = null;
    try {
      validateUser(user);
      setAuthoritiesParentUser(user);
    } catch (Exception e) {
      throw new ErrorResponseException(HttpStatus.BAD_REQUEST, e);
    }
    try {
      if (securityUserRepository.getSecurityUserByUsername(user.getUsername()) != null) {
        throw new ErrorResponseException(HttpStatus.BAD_REQUEST, new IllegalArgumentException(
            "User with username: '" + user.getUsername() + "' already exists"));
      }
      final SecurityUser createdUser = securityUserRepository.save(user);
      if (createdUser.getId() != null) {
        responseEntity = ResponseEntity
            .status(HttpStatus.CREATED.value())
            // TODO: do not send full user - only new id
            .body(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(createdUser));
      }
    } catch (Exception e) {
      throw new ErrorResponseException(HttpStatus.BAD_REQUEST, e);
    }
    return responseEntity;
  }

  private void validateUser(final SecurityUser user) {
    Preconditions.checkNotNull(user, "Received empty user");
    Preconditions.checkArgument(user.getId() == null, "User id must not be provided");
    Preconditions.checkArgument(StringUtils.isNotBlank(user.getUsername()),
        "Username must not be blank");
    Preconditions.checkNotNull(user.getUserType(),
        "User type must be one of 'external', or 'internal'");
    Preconditions.checkArgument(StringUtils.isNotBlank(user.getEmail()),
        "User email must not be blank");
    Preconditions.checkArgument(user.getAuthorities() != null
            && !user.getAuthorities().isEmpty()
            && user.getAuthorities().stream().allMatch(it -> StringUtils.isNotBlank(it.getAuthority())),
        "User must have at least one authority and authorities must be non blank");
  }

  /**
   * We need to set the parent user for authorities passed in via api, otherwise they don't save
   * appropriately.
   * TODO: can use custom json-deserialization for this or modify the setter in the SecurityUser instead
   */
  private void setAuthoritiesParentUser(SecurityUser user) {
    user.getAuthorities().forEach(it -> {
      if (it instanceof SecurityAuthority) {
        ((SecurityAuthority) it).setUser(user);
      } else {
        throw new IllegalArgumentException("Found invalid authority type");
      }
    });
  }

}
