package com.demo.security.spring.h2;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class TestH2 {

  @Autowired
  private DataSource dataSource;

  @Test
  void testH2() {
    JdbcTemplate template = new JdbcTemplate(dataSource);
    template.getDataSource();
  }

}
