package com.demo.security.spring.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.utils.TableNames;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

@Log4j2
public abstract class AbstractPostgresSchemaTest {

  @Autowired
  protected TestDataGenerator testDataGenerator;

  @Autowired
  protected SecurityUserRepository userRepository;

  @Autowired
  protected DataSource dataSource;

  protected JdbcTemplate jdbcTemplate;

  @BeforeEach
  void beforeEach() {
    if (dataSource == null) {
      log.error("No existing datasource for test");
    }
    if (jdbcTemplate == null) {
      jdbcTemplate = new JdbcTemplate(dataSource);
    } else {
      log.info("JDBC Template already initialized");
    }
  }

  void _testPostgresContainerOk(PostgreSQLContainer<?> postgres) {
    assertNotNull(postgres);
    assertTrue(postgres.isCreated());
    assertTrue(postgres.isRunning());

    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();

    testDataGenerator.generateExternalUser(username, password, true);

    assertNotNull(userRepository.getSecurityUserByUsername(username));
    assertTrue(jdbcTemplate.queryForObject("SELECT 1 FROM demo.security_users LIMIT 1", Boolean.class));
  }

  void _testTableNames() {
    List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM information_schema.tables"
        + " WHERE table_catalog = 'test-security-demo' AND table_schema = 'demo'");
    assertFalse(list.isEmpty());
    for (String tableName : TableNames.NAMES) {
      Map<String, Object> row = list.stream().filter(it -> tableName.equals(it.get("table_name"))).findFirst().orElse(null);
      assertNotNull(row, "Expected to find row for tableName " + tableName);
      log.info("Found table " + tableName);
      assertEquals("BASE TABLE", row.get("table_type"));
      assertEquals("YES", row.get("is_insertable_into"));
    }
  }

  void _testTableColumns() {

  }

}
