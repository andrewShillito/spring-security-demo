package com.demo.security.spring.authentication;

import com.demo.security.spring.utils.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * A simple example custom authentication success handler
 */
@Log4j2
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    log.info(() -> "Executing inside example custom authentication success handler");
    response.sendRedirect(Constants.DEFAULT_LOGIN_REDIRECT_URL);
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authentication) throws IOException, ServletException {
    AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain,
        authentication);
    log.info(() -> "Executing inside example custom authentication success handler");
    response.sendRedirect(Constants.DEFAULT_LOGIN_REDIRECT_URL);
  }
}
