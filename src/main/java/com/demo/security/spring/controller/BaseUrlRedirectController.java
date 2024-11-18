package com.demo.security.spring.controller;

import com.demo.security.spring.utils.Constants;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/")
public class BaseUrlRedirectController {

  @RequestMapping
  public void redirectToLoginOrOpenApiPage(HttpServletResponse httpServletResponse, Authentication authentication)
      throws IOException {
    if (authentication == null || !authentication.isAuthenticated()) {
      httpServletResponse.sendRedirect("/login");
    } else {
      httpServletResponse.sendRedirect(Constants.DEFAULT_LOGIN_REDIRECT_URL);
    }
  }

}
