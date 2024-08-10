package com.demo.security.spring.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mock.env.MockEnvironment;

@SpringBootTest
class CustomBasicAuthenticationEntryPointTest {

  @Autowired
  private Environment environment;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testExampleExtraHeader() {
    final CustomBasicAuthenticationEntryPoint entryPoint = new CustomBasicAuthenticationEntryPoint(
        objectMapper,
        environment,
        false
    ); // TODO: some testing using mock env props
    final var responseMock = mock(HttpServletResponse.class);
    final ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<Integer> errorIntCaptor = ArgumentCaptor.forClass(Integer.class);
    final ArgumentCaptor<String> bodyValueCaptor = ArgumentCaptor.forClass(String.class);

    doNothing().when(responseMock).setHeader(anyString(), headerValueCaptor.capture());
    try {
      doNothing().when(responseMock).sendError(errorIntCaptor.capture(), bodyValueCaptor.capture());
      // in existing simple example impl the request and auth exception are not used so can just pass null
      entryPoint.commence(null, responseMock, null);
      verify(responseMock, times(2)).setHeader(anyString(), anyString());
      verify(responseMock, times(1)).sendError(anyInt(), anyString());
      // test arguments
      final List<String> headerValues = headerValueCaptor.getAllValues();
      assertEquals(2, headerValues.size());
      assertEquals(headerValues.get(0), "Basic realm=\"http://localhost:8080\"");
      assertEquals(headerValues.get(1), "Example extra header value");
      assertEquals(HttpStatus.UNAUTHORIZED.value(), errorIntCaptor.getValue());

      // TODO: build expected body
      assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), "TODO");
    } catch (Exception e) {
      fail("Encountered unexpected exception", e);
    }
  }

}