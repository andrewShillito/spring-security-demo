package com.demo.security.spring.utils;

/**
 * Constants for spring profile names which are used in this demo application
 */
public class SpringProfileConstants {

  /** The default spring profile name */
  public static final String DEFAULT = "default";

  /**
   * A spring profile which uses an in memory user details service - disables docker-compose startup
   * of postgres and adminer
   */
  public static final String IN_MEMORY_USERS = "inMemoryUsers";

  /** A profile which uses postgres database */
  public static final String POSTGRES = "postgres";

  /** A profile which uses h2 database ( for test ) */
  public static final String H2 = "h2";

  /** The mock production profile */
  public static final String PRODUCTION = "prod";

  /** Profile to use spring-docker-compose to startup containers for the app to use ie: postgres */
  public static final String DOCKER_COMPOSE = "dockerCompose";

  /** Profile which turns on liquibase */
  public static final String LIQUIBASE = "liquibase";

}
