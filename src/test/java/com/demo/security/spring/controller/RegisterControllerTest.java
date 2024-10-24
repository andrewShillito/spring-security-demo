package com.demo.security.spring.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.controller.error.DuplicateUserException;
import com.demo.security.spring.controller.error.ValidationErrorDetailsResponse;
import com.demo.security.spring.model.SecurityGroup;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.model.UserCreationResponse;
import com.demo.security.spring.model.UserCreationRequest;
import com.demo.security.spring.repository.SecurityUserGroupRepository;
import com.demo.security.spring.utils.AuthorityGroups;
import com.demo.security.spring.utils.Constants;
import com.demo.security.spring.validation.IsValidPassword;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RegisterControllerTest extends AbstractControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TestDataGenerator testDataGenerator;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserDetailsManager userDetailsManager;

  @Autowired
  private SecurityUserGroupRepository securityUserGroupRepository;

  @Test
  void testCors() throws Exception {
    // cors testing needs specific handling for the /register endpoint
    UserCreationRequest userCreationRequest = testDataGenerator.randomUserCreationRequest();
    String validRequestBody = asRequestBody(userCreationRequest);
    assertNotNull(validRequestBody);

    final String exampleInvalidUrl = "http://www.someOtherSite.com";
    for (String invalidOrigin : List.of(exampleInvalidUrl, faker.internet().url())) {
      MvcResult invalidResult = mockMvc.perform(post(RegisterController.RESOURCE_PATH)
              .header("Origin", invalidOrigin)
              .contentType(MediaType.APPLICATION_JSON)
              .content(validRequestBody))
          .andExpect(status().isForbidden())
          .andReturn();
      assertEquals("Invalid CORS request", invalidResult.getResponse().getContentAsString());
      // invalid options request
      invalidResult = mockMvc.perform(options(RegisterController.RESOURCE_PATH)
          .header("Access-Control-Request-Method", "GET")
          .header("Origin", invalidOrigin)
      ).andReturn();
      assertEquals(403, invalidResult.getResponse().getStatus());
      assertEquals("Invalid CORS request", invalidResult.getResponse().getContentAsString());
    }

    // allowed cors requests
    for (String origin : Constants.EXAMPLE_ALLOWED_CORS_PATHS) {
      userCreationRequest = testDataGenerator.randomUserCreationRequest();
      validRequestBody = asRequestBody(userCreationRequest);
      assertNotNull(validRequestBody);
      var response = mockMvc.perform(post(RegisterController.RESOURCE_PATH)
              .header("Origin", origin)
              .with(csrf())
              .contentType(MediaType.APPLICATION_JSON)
              .content(validRequestBody))
          .andExpect(status().isCreated())
          .andReturn()
          .getResponse();
      DemoAssertions.assertExpectedUserCreated(userCreationRequest.getUsername(), asUserCreationResponse(response.getContentAsString()));
    }
  }

  @Test
  void attemptRegisterUserWithNoCsrfToken() throws Exception {
    UserCreationRequest request = testDataGenerator.randomUserCreationRequest();
    var response = mockMvc.perform(post(RegisterController.RESOURCE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(Objects.requireNonNull(asRequestBody(request))))
        .andExpect(status().isFound())
        .andExpect(redirectedUrl(Constants.INVALID_SESSION_URL))
        .andReturn()
        .getResponse();
    assertNotNull(response);
    DemoAssertions.assertEmpty(response.getContentAsString());
    DemoAssertions.assertHasNoSessionToken(response);
  }

  @Test
  void registerUserWithCsrfToken() throws Exception {
    UserCreationRequest request = testDataGenerator.randomUserCreationRequest();
    final ZonedDateTime creationRequestTime = ZonedDateTime.now();
    var response = mockMvc.perform(post(RegisterController.RESOURCE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)).with(csrf())
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse();
    assertNotNull(response);
    DemoAssertions.assertNotEmpty(response.getContentAsString());
    DemoAssertions.assertHasNoCsrftoken(response);
    DemoAssertions.assertHasNoSessionToken(response);
    UserCreationResponse userCreationResponse = asUserCreationResponse(response.getContentAsString());
    assertNotNull(userCreationResponse);
    DemoAssertions.assertExpectedUserCreated(request.getUsername(), userCreationResponse);

    SecurityUser user = (SecurityUser) userDetailsManager.loadUserByUsername(request.getUsername());
    assertNotNull(user);

    // now we login as that newly registered user using form login
    DemoAssertions.assertFormLoginSuccessful(mockMvc, request.getUsername(), request.getPassword());
    var userDetails = mockMvc.perform(get(UserController.RESOURCE_PATH).with(csrf()).with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();
    SecurityUser createdSecurityUser = objectMapper.readValue(userDetails.getContentAsString(), SecurityUser.class);
    assertEquals(request.getUsername(), createdSecurityUser.getUsername());
    assertEquals(request.getEmail(), createdSecurityUser.getEmail());
    assertNotNull(createdSecurityUser.getId());
    assertNull(createdSecurityUser.getLockedDate());
    assertEquals(0, createdSecurityUser.getFailedLoginAttempts());
    assertNull(createdSecurityUser.getPassword()); // this is because password is write only
    assertNull(createdSecurityUser.getPasswordExpiredDate());
    assertFalse(createdSecurityUser.isPasswordExpired());
    assertNotNull(createdSecurityUser.getControlDates());
    DemoAssertions.assertDateIsNowIsh(createdSecurityUser.getControlDates().getCreated());
    DemoAssertions.assertDateIsNowIsh(createdSecurityUser.getControlDates().getLastUpdated());
    DemoAssertions.assertDateFuzzyEquals(creationRequestTime, createdSecurityUser.getControlDates().getCreated());
    DemoAssertions.assertDateFuzzyEquals(creationRequestTime, createdSecurityUser.getControlDates().getLastUpdated());
    assertTrue(createdSecurityUser.isEnabled());
    assertFalse(createdSecurityUser.isLocked());
    assertNull(createdSecurityUser.getUnlockDate());
    assertTrue(createdSecurityUser.isAccountNonLocked());
    assertEquals(0, createdSecurityUser.getNumPreviousLockouts());
    assertFalse(createdSecurityUser.isAccountExpired());
    assertTrue(createdSecurityUser.isCredentialsNonExpired());
    assertEquals(2, createdSecurityUser.getGroups().size());
    assertEquals(16, createdSecurityUser.getAuthorities().size());
    assertIterableEquals(
        new HashSet<>(List.of(AuthorityGroups.GROUP_USER, AuthorityGroups.GROUP_ACCOUNT_HOLDER)),
        createdSecurityUser.getGroups().stream().map(SecurityGroup::getCode).collect(Collectors.toSet())
    );
    DemoAssertions.assertFormLogoutSuccessful(mockMvc);
  }

  @Test
  void attemptToRegisterInvalidUser() throws Exception {
    UserCreationRequest request = testDataGenerator.randomUserCreationRequest();

    final List<String> invalidPasswords = List.of(
        "abCd12$",
        "abcd123$",
        "abcD1234",
        " abcD123$ "
    );

    final List<String> invalidUsernames = List.of(
        "",
        " ",
        "   "
    );

    // usernames and emails are both just non-blank but this may change
    final List<String> invalidEmails = invalidUsernames;

    for (String password: invalidPasswords) {
      request.setPassword(password);
      var validationErrors = expectBadRequest(request);
      assertEquals(1, validationErrors.length);
      assertEquals("password", validationErrors[0].getFieldName());
      assertEquals(IsValidPassword.class.getSimpleName(), validationErrors[0].getErrorCode());
      assertEquals(password, validationErrors[0].getRejectedValue());
      DemoAssertions.assertNotBlank(validationErrors[0].getErrorMessage());
      DemoAssertions.assertFormLoginUnSuccessful(mockMvc, request.getUsername(), request.getPassword());
      assertThrows(UsernameNotFoundException.class, () -> userDetailsManager.loadUserByUsername(request.getUsername()));
    }

    // reset to valid password
    request.setPassword(testDataGenerator.randomPassword());

    for (String username: invalidUsernames) {
      request.setUsername(username);
      var validationErrors = expectBadRequest(request);
      assertEquals(2, validationErrors.length);
      assertEquals("username", validationErrors[0].getFieldName());
      assertEquals("username", validationErrors[1].getFieldName());
      assertEquals(username, validationErrors[0].getRejectedValue());
      assertEquals(username, validationErrors[1].getRejectedValue());
      DemoAssertions.assertNotBlank(validationErrors[0].getErrorMessage());
      DemoAssertions.assertNotBlank(validationErrors[1].getErrorMessage());
      assertNotNull(Arrays.stream(validationErrors).filter(it -> "NotBlank".equals(it.getErrorCode())).findFirst().orElse(null));
      assertNotNull(Arrays.stream(validationErrors).filter(it -> "Size".equals(it.getErrorCode())).findFirst().orElse(null));
      DemoAssertions.assertFormLoginUnSuccessful(mockMvc, request.getUsername(), request.getPassword());
      assertThrows(IllegalArgumentException.class, () -> userDetailsManager.loadUserByUsername(request.getUsername()));
    }

    // reset to valid username
    request.setUsername(testDataGenerator.randomUsername());

    // usernames and emails have same not-blank validation rules
    for (String email : invalidEmails) {
      request.setEmail(email);
      var validationErrors = expectBadRequest(request);
      if (validationErrors.length == 0 || validationErrors.length > 2) {
        fail("Found unexpected length of validation errors " + validationErrors.length + " " + Arrays.toString(validationErrors));
      }
      assertEquals("email", validationErrors[0].getFieldName());
      assertEquals(email, validationErrors[0].getRejectedValue());
      DemoAssertions.assertNotBlank(validationErrors[0].getErrorMessage());
      assertNotNull(Arrays.stream(validationErrors).filter(it -> "NotBlank".equals(it.getErrorCode())).findFirst().orElse(null));
      if (validationErrors.length == 2) {
        assertEquals("email", validationErrors[1].getFieldName());
        assertEquals(email, validationErrors[1].getRejectedValue());
        DemoAssertions.assertNotBlank(validationErrors[1].getErrorMessage());
        assertNotNull(Arrays.stream(validationErrors).filter(it -> "Email".equals(it.getErrorCode())).findFirst().orElse(null));
      }
      DemoAssertions.assertFormLoginUnSuccessful(mockMvc, request.getUsername(), request.getPassword());
      assertThrows(UsernameNotFoundException.class, () -> userDetailsManager.loadUserByUsername(request.getUsername()));
    }
  }

  @Test
  void testCreateDuplicateUser() throws Exception {
    UserCreationRequest request = testDataGenerator.randomUserCreationRequest();
    mockMvc.perform(post(RegisterController.RESOURCE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)).with(csrf())
        )
        .andExpect(status().isCreated())
        .andReturn()
        .getResponse();
    var response = mockMvc.perform(post(RegisterController.RESOURCE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)).with(csrf())
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse();
    ValidationErrorDetailsResponse errorDetails = objectMapper.readValue(response.getContentAsString(), ValidationErrorDetailsResponse.class);
    assertNotNull(errorDetails);
    assertEquals("username", errorDetails.getFieldName());
    assertEquals(request.getUsername(), errorDetails.getRejectedValue());
    assertEquals(DuplicateUserException.ERROR_CODE, errorDetails.getErrorCode());
    assertEquals(String.format(DuplicateUserException.ERROR_MESSAGE_TEMPLATE, request.getUsername()), errorDetails.getErrorMessage());
  }

  private ValidationErrorDetailsResponse[] expectBadRequest(UserCreationRequest request)
      throws Exception {
    var response = mockMvc.perform(post(RegisterController.RESOURCE_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)).with(csrf())
        )
        .andExpect(status().isBadRequest())
        .andReturn()
        .getResponse();
    assertNotNull(response);
    DemoAssertions.assertNotEmpty(response.getContentAsString());
    return objectMapper.readValue(response.getContentAsString(), ValidationErrorDetailsResponse[].class);
  }

  private UserCreationResponse asUserCreationResponse(String content) {
    try {
      return objectMapper.readValue(content, UserCreationResponse.class);
    } catch (Exception e) {
      fail("Failed to deserialize UserCreationResponse from content: " + content, e);
    }
    return null;
  }

  private String asRequestBody(UserCreationRequest request) {
    try {
      return objectMapper.writeValueAsString(request);
    } catch (Exception e) {
      fail("Failed to turn UserCreationRequest into JSON! " + request, e);
    }
    return null;
  }

}
