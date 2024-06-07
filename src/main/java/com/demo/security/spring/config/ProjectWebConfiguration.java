package com.demo.security.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ProjectWebConfiguration implements WebMvcConfigurer {

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    // deprecated but will use until spring-boot release containing UrlHandlerFilter is available
    // see https://github.com/spring-projects/spring-framework/commit/edb6bb717d9ea10429a9e5c1fba285cd7761d5a1
    configurer.setUseTrailingSlashMatch(true);
  }
}
