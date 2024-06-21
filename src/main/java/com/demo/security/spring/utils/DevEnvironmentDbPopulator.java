package com.demo.security.spring.utils;

import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.service.ExampleUsersManager;
import java.util.List;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
@Log4j2
public class DevEnvironmentDbPopulator {

  private ExampleUsersManager exampleUsersManager;

  private SecurityUserRepository securityUserRepository;

  private PasswordEncoder passwordEncoder;

  @EventListener(ContextRefreshedEvent.class)
  public void seedDatabaseIfEmpty() {
    if (securityUserRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment users as the table already contains data");
    } else {
      log.info(() -> "Populating development environment security users.");
      List<SecurityUser> users = exampleUsersManager.getDevEnvironmentUsers();
      try {
        securityUserRepository.saveAll(users);
        log.info(() -> "Finished populating " + users.size() + " development environment users");
      } catch (Exception e) {
        throw new RuntimeException("Failed to populate development environment security users with error!", e);
      }
    }
  }

}
