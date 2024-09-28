package com.demo.security.spring.postgres;

import com.demo.security.spring.utils.SpringProfileConstants;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Tests application db schema and basic application actions using db created via schema.sql.
 * Besides asserting expected schema, this test and {@link PostgresSchemaHibernateTest} together
 * assert jpa config and schema.sql produce identical results and can be used interchangeably.
 */
@SpringBootTest
@ActiveProfiles(value = { SpringProfileConstants.DEFAULT, SpringProfileConstants.POSTGRES })
@Testcontainers
@Log4j2
public class PostgresSchemaFileTest extends AbstractPostgresSchemaTest {

  @Container
  private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
      .withDatabaseName("test-security-demo");

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Test
  void testCanUsePostgresDuringTest() {
    _testPostgresContainerOk(postgres);
  }

  @Test
  void testTableNames() {
    _testTableNames();
  }

  @Test
  void testTableColumns() {
    _testTableColumns();
  }

}
