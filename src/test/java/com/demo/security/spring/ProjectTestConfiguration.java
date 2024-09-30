package com.demo.security.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectTestConfiguration {

  public static final String PROPERTY_CUSTOM_DB_SCHEMA_VALIDATION = "custom.db.schema.validation";

  @Bean
  public TestDataGenerator testDataGenerator() {
    return new TestDataGenerator();
  }

}
