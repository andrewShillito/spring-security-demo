package com.demo.security.spring.authentication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.controller.error.AuthErrorDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CustomAccessDeniedHandlerTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockUser
  void testAccessDeniedHandlerWithMockMvc() throws Exception {
    final String expectedUri = "/anEndPointThatDoesntExist";
    var response = mockMvc.perform(get(expectedUri))
        .andExpect(status().isForbidden())
        .andReturn()
        .getResponse();
    var expected = AuthErrorDetailsResponse.builder()
        .errorCode(HttpStatus.FORBIDDEN.value())
        .errorMessage("Access Denied")
        .requestUri(expectedUri)
        .additionalInfo("Example additional info")
        .time(ZonedDateTime.now())
        .build();
    DemoAssertions.assertAuthErrorEquals(expected, objectMapper.readValue(response.getContentAsString(), AuthErrorDetailsResponse.class));
  }

}