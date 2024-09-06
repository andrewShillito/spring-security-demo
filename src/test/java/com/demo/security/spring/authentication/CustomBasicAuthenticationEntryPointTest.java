package com.demo.security.spring.authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.controller.error.AuthErrorDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.CredentialsExpiredException;

@SpringBootTest
class CustomBasicAuthenticationEntryPointTest {

  @Autowired
  private Environment environment;

  @Test
  void testResponseWithNullAuthException() throws IOException {
    final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
    final ObjectWriter mockWriter = mock(ObjectWriter.class);
    final ArgumentCaptor<AuthErrorDetailsResponse> errorDetailsCaptor = ArgumentCaptor.forClass(
        AuthErrorDetailsResponse.class);
    when(mockObjectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
    doNothing().when(mockWriter).writeValue(any(OutputStream.class), errorDetailsCaptor.capture());

    final CustomBasicAuthenticationEntryPoint entryPoint = new CustomBasicAuthenticationEntryPoint(
        mockObjectMapper,
        environment,
        false
    );

    final String serverPort = environment.getProperty("server.port", "8080");
    final var responseMock = mock(HttpServletResponse.class);
    final ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

    doNothing().when(responseMock).setHeader(anyString(), headerValueCaptor.capture());
    try {
      when(responseMock.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
      // in existing simple example impl the request and auth exception are not used so can just pass null
      entryPoint.commence(null, responseMock, null);
      verify(responseMock, times(2)).setHeader(anyString(), anyString());
      verify(responseMock, times(1)).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(responseMock, times(1)).getOutputStream();
      // test arguments
      final List<String> headerValues = headerValueCaptor.getAllValues();
      assertEquals(2, headerValues.size());
      assertEquals(headerValues.get(0), "Basic realm=\"http://localhost:8080\"");
      assertEquals(headerValues.get(1), "Example extra header value");
      AuthErrorDetailsResponse actual = errorDetailsCaptor.getValue();
      assertNotNull(actual, "Expected non-null actual response from body " + actual);
      DemoAssertions.assertAuthErrorEquals(defaultExpected(serverPort), actual);
    } catch (Exception e) {
      fail("Encountered unexpected exception", e);
    }
  }

  @Test
  void testResponseWithAuthException() throws IOException {
    final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
    final ObjectWriter mockWriter = mock(ObjectWriter.class);
    final ArgumentCaptor<AuthErrorDetailsResponse> errorDetailsCaptor = ArgumentCaptor.forClass(
        AuthErrorDetailsResponse.class);
    when(mockObjectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
    doNothing().when(mockWriter).writeValue(any(OutputStream.class), errorDetailsCaptor.capture());

    final CustomBasicAuthenticationEntryPoint entryPoint = new CustomBasicAuthenticationEntryPoint(
        mockObjectMapper,
        environment,
        false
    );

    final var requestMock = mock(HttpServletRequest.class);
    final var responseMock = mock(HttpServletResponse.class);
    when(responseMock.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
    final ArgumentCaptor<String> headerValueCaptor = ArgumentCaptor.forClass(String.class);

    final String expectedMessage = "Authentication credentials not found";
    final String expectedScheme = "https";
    final String expectedServerName = "testAuthenticationEntryPoint";
    final String expectedServerPort = "1234";
    final String expectedRealm = expectedScheme + "://" + expectedServerName + ":" + expectedServerPort;

    doNothing().when(responseMock).setHeader(anyString(), headerValueCaptor.capture());
    try {
      when(requestMock.getRequestURI()).thenReturn(expectedRealm);
      when(requestMock.getScheme()).thenReturn(expectedScheme);
      when(requestMock.getServerPort()).thenReturn(Integer.valueOf(expectedServerPort));
      when(requestMock.getServerName()).thenReturn(expectedServerName);
      // in existing simple example impl the request and auth exception are not used so can just pass null
      entryPoint.commence(requestMock, responseMock, new AuthenticationCredentialsNotFoundException(expectedMessage));
      verify(responseMock, times(2)).setHeader(anyString(), anyString());
      verify(responseMock, times(1)).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(responseMock, times(1)).getOutputStream();
      verify(requestMock, times(1)).getRequestURI();
      // test arguments
      final List<String> headerValues = headerValueCaptor.getAllValues();
      assertEquals(2, headerValues.size());
      assertEquals(headerValues.get(0), "Basic realm=\"" + expectedRealm + "\"");
      assertEquals(headerValues.get(1), "Example extra header value");

      var expected = defaultExpected(expectedServerPort);
      expected.setErrorMessage(expectedMessage);
      expected.setRequestUri(expectedRealm);
      expected.setRealm(expectedRealm);

      final var actual = errorDetailsCaptor.getValue();
      assertNotNull(actual, "Expected non-null actual response from body " + actual);
      DemoAssertions.assertAuthErrorEquals(expected, actual);
    } catch (Exception e) {
      fail("Encountered unexpected exception", e);
    }
  }

  @Test
  void testFailToMakeResponseBody() throws IOException {
    final var objectMapperMock = mock(ObjectMapper.class);
    final var environmentMock = mock(Environment.class);

    when(objectMapperMock.writerWithDefaultPrettyPrinter()).thenThrow(new RuntimeException());
    when(environmentMock.getProperty(anyString())).thenReturn("1234");

    final CustomBasicAuthenticationEntryPoint entryPoint = new CustomBasicAuthenticationEntryPoint(
        objectMapperMock,
        environmentMock,
        true
    );
    final var responseMock = mock(HttpServletResponse.class);
    doNothing().when(responseMock).setHeader(anyString(), anyString());
    doNothing().when(responseMock).sendError(anyInt(), anyString());
    assertThrows(RuntimeException.class, () -> entryPoint.commence(null, responseMock, new CredentialsExpiredException("Credentials expired")));
    verify(environmentMock, times(1)).getProperty(anyString(), anyString());
  }

  @Test
  void testProdProfile() throws ServletException, IOException {
    final ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
    final ObjectWriter mockWriter = mock(ObjectWriter.class);
    final ArgumentCaptor<AuthErrorDetailsResponse> errorDetailsCaptor = ArgumentCaptor.forClass(
        AuthErrorDetailsResponse.class);
    when(mockObjectMapper.writerWithDefaultPrettyPrinter()).thenReturn(mockWriter);
    doNothing().when(mockWriter).writeValue(any(OutputStream.class), errorDetailsCaptor.capture());

    final var environmentMock = mock(Environment.class);
    final String expectedPort = "1234";
    when(environmentMock.getProperty(anyString(), anyString())).thenReturn(expectedPort);
    final CustomBasicAuthenticationEntryPoint entryPoint = new CustomBasicAuthenticationEntryPoint(
        mockObjectMapper,
        environmentMock,
        true
    );

    final var responseMock = mock(HttpServletResponse.class);
    doNothing().when(responseMock).setHeader(anyString(), anyString());
    doNothing().when(responseMock).setStatus(anyInt());
    when(responseMock.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

    entryPoint.commence(null, responseMock, new CredentialsExpiredException("Credentials expired"));

    verify(environmentMock, times(1)).getProperty(anyString(), anyString());
    verify(responseMock, times(2)).setHeader(anyString(), anyString());
    verify(responseMock, times(1)).setStatus(HttpStatus.UNAUTHORIZED.value());
    verify(responseMock, times(1)).getOutputStream();

    var expected = defaultExpected("1234");
    expected.setErrorMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase());
    DemoAssertions.assertAuthErrorEquals(expected, errorDetailsCaptor.getValue());
  }

  @Test
  void testConstructor() {
    assertThrows(NullPointerException.class, () -> new CustomBasicAuthenticationEntryPoint(null, new MockEnvironment(), false));
    assertThrows(NullPointerException.class, () -> new CustomBasicAuthenticationEntryPoint(null, new MockEnvironment(), true));
    assertThrows(NullPointerException.class, () -> new CustomBasicAuthenticationEntryPoint(new ObjectMapper(), null, false));
    assertThrows(NullPointerException.class, () -> new CustomBasicAuthenticationEntryPoint(new ObjectMapper(), null, true));
  }

  private AuthErrorDetailsResponse defaultExpected(String serverPort) {
    return AuthErrorDetailsResponse.builder()
        .time(ZonedDateTime.now())
        .realm("http://localhost:" + serverPort)
        .errorCode(HttpStatus.UNAUTHORIZED.value())
        .errorMessage(HttpStatus.UNAUTHORIZED.getReasonPhrase())
        .requestUri("http://localhost:" + serverPort)
        .additionalInfo("Example additional info")
        .build();
  }
}