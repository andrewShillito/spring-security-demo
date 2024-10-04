package com.demo.security.spring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
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
public class OpenApiTest extends AbstractControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TestDataGenerator testDataGenerator;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testUnauthenticated() throws Exception {
    _testSecuredBaseUrlAuth(mockMvc, Constants.SWAGGER_UI_URL);
    _testSecuredBaseUrlAuth(mockMvc, Constants.SWAGGER_SCHEMA_URL);
  }

  @Test
  void testSwaggerUi() throws Exception {
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    SecurityUser securityUser = testDataGenerator.generateExternalUser(username, password, true);
    mockMvc.perform(get(Constants.SWAGGER_UI_URL).with(user(securityUser)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.TEXT_HTML));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testSwaggerSchema() throws Exception {
    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();
    SecurityUser securityUser = testDataGenerator.generateExternalUser(username, password, true);
    Map<String, Object> swaggerSchema = getSwaggerSchema(mockMvc, securityUser);
    assertNotNull(swaggerSchema);
    assertFalse(swaggerSchema.isEmpty());
    // assert existing global open api schema details
    assertEquals("3.0.1", swaggerSchema.get("openapi"));
    assertInstanceOf(Map.class, swaggerSchema.get("info"));
    assertEquals("OpenAPI definition", ((Map) swaggerSchema.get("info")).get("title"));
    assertEquals("v0", ((Map) swaggerSchema.get("info")).get("version"));
    assertInstanceOf(List.class, swaggerSchema.get("servers"));
    assertFalse(((List) swaggerSchema.get("servers")).isEmpty());
    assertInstanceOf(Map.class, ((List) swaggerSchema.get("servers")).getFirst());
    // these below are probably not real world values but are applicable to local env
    assertEquals("http://localhost", ((Map) ((List) swaggerSchema.get("servers")).getFirst()).get("url"));
    assertEquals("Generated server url", ((Map) ((List) swaggerSchema.get("servers")).getFirst()).get("description"));
  }
}
