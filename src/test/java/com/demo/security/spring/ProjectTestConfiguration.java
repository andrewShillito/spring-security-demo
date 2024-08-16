package com.demo.security.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectTestConfiguration {

  @Bean
  public TestDataGenerator testDataGenerator() {
    return new TestDataGenerator();
  }

}
