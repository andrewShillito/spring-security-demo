package com.demo.security.spring.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.model.SecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest extends AbstractControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TestDataGenerator testDataGenerator;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testUnauthenticated() throws Exception {
    _testSecuredBaseUrlAuth(mockMvc, UserController.RESOURCE_PATH);
  }

  @Test
  void testGetValidUser() throws Exception {
    final String username = testDataGenerator.randomUsername();
    final String userRawPassword = testDataGenerator.randomPassword();
    SecurityUser generatedUser = testDataGenerator.generateExternalUser(username, userRawPassword, true);
    var response = mockMvc.perform(get(UserController.RESOURCE_PATH).with(user(generatedUser)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn()
        .getResponse();
    DemoAssertions.assertNotEmpty(response.getContentAsString());
    DemoAssertions.assertUsersEqual(generatedUser, objectMapper.readValue(response.getContentAsString(), SecurityUser.class));
  }
}
