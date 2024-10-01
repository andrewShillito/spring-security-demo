package com.demo.security.spring.postgres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.TestDataGenerator;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.utils.TableNames;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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

  @Autowired
  protected JdbcTemplate jdbcTemplate;

  @Value("${spring.jpa.properties.hibernate.default_schema:demo}")
  private String TEST_SCHEMA_NAME;

  @Autowired
  protected Environment environment;

  public static final String TEST_DB_NAME = "test-security-demo";

  public static final String POSTGRES_IMAGE_NAME = "postgres:17.0-bookworm";

  /** Stores map of:
   * key    - type ie: columns, indexes
   * value  - Map<String, List<Map<String, Object>> rows from query for the columns */
  public static AtomicReference<Map<String, Map<String, List<Map<String, Object>>>>> DATA = new AtomicReference<>(new HashMap<>());

  /** SQL query to use for retrieving column definitions */
  public static String COLUMNS_QUERY = "SELECT * FROM information_schema.columns"
      + " WHERE table_name = ? ORDER BY column_name";

  public static String INDEXES_QUERY = "SELECT * FROM pg_indexes WHERE schemaname = ? AND tablename = ? ORDER BY indexname;";

  protected void _testPostgresContainerOk(PostgreSQLContainer<?> postgres) {
    assertNotNull(postgres);
    assertTrue(postgres.isCreated());
    assertTrue(postgres.isRunning());

    final String username = testDataGenerator.randomUsername();
    final String password = testDataGenerator.randomPassword();

    testDataGenerator.generateExternalUser(username, password, true);

    assertNotNull(userRepository.getSecurityUserByUsername(username));
    Boolean usersTableExists = jdbcTemplate.queryForObject("SELECT 1 FROM information_schema.tables"
            + " WHERE table_schema = ? AND table_name = ? LIMIT 1", Boolean.class, TEST_SCHEMA_NAME, TableNames.USERS);
    assertNotNull(usersTableExists);
    assertTrue(usersTableExists);
  }

  protected void _testTableNames() {
    List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM information_schema.tables"
        + " WHERE table_catalog = ? AND table_schema = ?", TEST_DB_NAME, TEST_SCHEMA_NAME);
    assertFalse(list.isEmpty());
    for (String tableName : TableNames.NAMES) {
      Map<String, Object> row = list.stream().filter(it -> tableName.equals(it.get("table_name"))).findFirst().orElse(null);
      assertNotNull(row, "Expected to find row for tableName " + tableName);
      log.info("Found table " + tableName);
      assertEquals("BASE TABLE", row.get("table_type"));
      assertEquals("YES", row.get("is_insertable_into"));
    }
  }

  protected void _testTableColumns() {
    for (String tableName : TableNames.NAMES) {
      List<Map<String, Object>> columns = jdbcTemplate.queryForList("SELECT * FROM information_schema.columns"
          + " WHERE table_name = ?", tableName);
      DemoAssertions.assertNotEmpty(columns);
    }
  }

  protected Map<String, List<Map<String, Object>>> getColumnsInfo() {
    Map<String, List<Map<String, Object>>> result = new HashMap<>();
    for (String tableName : TableNames.NAMES) {
      List<Map<String, Object>> columns = jdbcTemplate.queryForList(COLUMNS_QUERY, tableName);
      DemoAssertions.assertNotEmpty(columns);
      for (var row : columns) {
        row.remove("ordinal_position");
        row.remove("dtd_identifier");
        row.remove("column_default");
      }
      result.put(tableName, columns);
    }
    return result;
  }

  protected Map<String, List<Map<String, Object>>> getIndexes() {
    Map<String, List<Map<String, Object>>> result = new HashMap<>();
    for (String tableName : TableNames.NAMES) {
      List<Map<String, Object>> indexes = jdbcTemplate.queryForList(INDEXES_QUERY, TEST_SCHEMA_NAME, tableName);
      DemoAssertions.assertNotEmpty(indexes);
      result.put(tableName, indexes);
    }
    return result;
  }
}
