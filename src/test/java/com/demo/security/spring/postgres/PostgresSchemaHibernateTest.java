package com.demo.security.spring.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.utils.SpringProfileConstants;
import com.demo.security.spring.utils.TableNames;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.AbstractImagePullPolicy;
import org.testcontainers.images.ImageData;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Tests application db schema and basic application actions using db created via hibernate auto-init
 * based on the java entity annotations. Besides asserting expected schema, this test and {@link PostgresSchemaFileTest}
 * together assert jpa config and schema.sql produce identical results and can be used interchangeably.
 */
@SpringBootTest
@ActiveProfiles(value = { SpringProfileConstants.DEFAULT, SpringProfileConstants.POSTGRES, SpringProfileConstants.NO_SCHEMA_INIT })
@Testcontainers
@Log4j2
@Order(2)
public class PostgresSchemaHibernateTest extends AbstractPostgresSchemaTest {

  @Container
  private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME)
      .withDatabaseName(TEST_DB_NAME);

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("example-data.enabled", () -> false); // turn off seeding of example data - will test that elsewhere
  }

  @Test
  @Order(1)
  void testCanUsePostgresDuringTest() {
    _testPostgresContainerOk(postgres);
  }

  @Test
  @Order(2)
  void proofOfConcept() {
    if (DATA != null && DATA.get().get("columns") != null) {
      log.info("Validating generated vs. schema.sql created table columns");
      assertNotNull(DATA);
      Map<String, List<Map<String, Object>>> result = getColumnsInfo();
      var columnsData = DATA.get().get("columns");
      assertNotNull(columnsData);
      for (var entry : columnsData.entrySet()) {
        assertIterableEquals(entry.getValue(), result.get(entry.getKey()));
      }

      Map<String, List<Map<String, Object>>> indexData = DATA.get().get("indexes");
      Map<String, List<Map<String, Object>>> indexes = getIndexes();
      if (indexData != null && indexes != null) {
        assertEquals(indexData.size(), indexes.size());
        for (var entry : indexData.entrySet()) {
          log.info("Validating " + entry + " against " + indexes.get(entry.getKey()));
          assertIterableEquals(entry.getValue(), indexes.get(entry.getKey()));
        }
      }
    } else {
      log.info("Not running custom db schema validation as DATA from postgres initialized with SQL is null. This test is being run alone.");
    }
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
