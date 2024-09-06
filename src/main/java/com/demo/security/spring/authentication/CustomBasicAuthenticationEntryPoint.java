package com.demo.security.spring.authentication;

import com.demo.security.spring.controller.error.AuthErrorDetailsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * An example custom basic authentication entry point based on {@link org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint}
 */
@Log4j2
public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  private final Environment environment;

  private final boolean isProd;

  public CustomBasicAuthenticationEntryPoint(ObjectMapper objectMapper, Environment environment, boolean isProd) {
    Preconditions.checkNotNull(objectMapper, "Object mapper cannot be null");
    Preconditions.checkNotNull(environment, "Environment cannot be null");
    this.objectMapper = objectMapper;
    this.environment = environment;
    this.isProd = isProd;
  }

  /**
   * Adds an extra example header "example-extra-header" to the response and provides a hopefully helpful
   * json response body. Note that in production profile, the sending of error message is limited for security
   * purposes.
   * @param request that resulted in an <code>AuthenticationException</code>
   * @param response so that the user agent can begin authentication
   * @param authException that caused the invocation
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    final String realm = getRealm(request);
    response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
    response.setHeader("example-extra-header", "Example extra header value");
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    writeResponseBody(request, response, authException, realm);
  }

  protected void writeResponseBody(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authException,
      final String realm) {
    try {
      objectMapper.writerWithDefaultPrettyPrinter()
          .writeValue(
              response.getOutputStream(),
              AuthErrorDetailsResponse.builder()
                  .time(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")))
                  .realm(realm)
                  .errorCode(HttpStatus.UNAUTHORIZED.value())
                  .errorMessage(!isProd && authException != null ? authException.getMessage() : HttpStatus.UNAUTHORIZED.getReasonPhrase())
                  .requestUri(request != null ? request.getRequestURI() : realm)
                  .additionalInfo("Example additional info")
                  .build());
    } catch (Exception e) {
      throw new RuntimeException("Failed to produce AuthErrorDetailsResponse with error ", e);
    }
  }

  /**
   * Returns an example realm for the WWW-Authenticate response header.
   * Just sends the uri for the demo app as that is the most helpful thing to do here currently
   * without a live domain.
   * @param request the http request
   * @return an example realm
   */
  protected String getRealm(final HttpServletRequest request) {
    /*
     * Example realm names for kerberos ( not that this app uses kerberos ):
     * domain: ATHENA.MIT.EDU
     * X500:   C=US/O=OSF
     * other:  NAMETYPE:rest/of.name=without-restrictions
     * see https://www.gnu.org/software/shishi/manual/html_node/Realm-and-Principal-Naming.html
     * */
    // just returning an example
    if (request == null) {
      // really only happens during testing...
      return "http://localhost:" + environment.getProperty("server.port", "8080");
    } else {
      // would be nice also to use an apache URIBuilder or okHttp HttpUrl.Builder() but not really worth adding
      // those to the project for just this method
      return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
  }
}
