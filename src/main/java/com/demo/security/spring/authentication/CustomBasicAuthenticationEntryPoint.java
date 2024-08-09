package com.demo.security.spring.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * An example custom basic authentication entry point based on {@link org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint}
 * which adds an extra header in the response
 */
@Log4j2
public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

  /**
   * Adds an extra example header "example-extra-header" to the response.
   * @param request that resulted in an <code>AuthenticationException</code>
   * @param response so that the user agent can begin authentication
   * @param authException that caused the invocation
   * @throws IOException
   * @throws ServletException
   */
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {
    /*
    * Example realm names for kerberos ( not that this app uses kerberos ):
    * domain: ATHENA.MIT.EDU
    * X500:   C=US/O=OSF
    * other:  NAMETYPE:rest/of.name=without-restrictions
    * see https://www.gnu.org/software/shishi/manual/html_node/Realm-and-Principal-Naming.html
    * */
    // there is no domain for this example app so using an example X.500 format:
    final String exampleRealm = "C=US/O=DEMO";
    response.setHeader("WWW-Authenticate", "Basic realm=\"" + exampleRealm + "\"");
    response.setHeader("example-extra-header", "Example extra header value");
    response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
  }
}
