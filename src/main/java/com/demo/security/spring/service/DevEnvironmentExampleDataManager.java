package com.demo.security.spring.service;

import com.demo.security.spring.model.SecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Handles retrieving lists of example users and other data from resource files for local dev environment testing.
 */
@Log4j2
@Builder
@Setter
public class DevEnvironmentExampleDataManager {

  private PasswordEncoder passwordEncoder;

  private ObjectMapper objectMapper;

  private ExampleDataGenerationService generationService;

  private boolean regenerateData;

  /**
   * Returns a list of UserDetails from the result of {@link #getDevEnvironmentUsers()}
   * which reads a json file of example {@link SecurityUser} records.
   * Encodes the example users passwords with the provided encoder.
   * @return list of user details object to be seeded into in-memory user details manager
   */
  public List<UserDetails> getInMemoryUsers() {
    final List<SecurityUser> securityUsers = getDevEnvironmentUsers();
    return securityUsers
        .stream()
        .map(it -> (UserDetails) it)
        .collect(Collectors.toList());
  }

  /**
   * Reads a json file for users to seed into the database.
   * Because hibernate and data.sql don't always get along and this simplifies the handling of
   * user seeding by allowing the app to check if users exist before we try to seed them again.
   * Should not be used in any production environment.
   * @return a List of security users to populate into the database
   */
  public List<SecurityUser> getDevEnvironmentUsers() {
    List<SecurityUser> securityUsers;
    if (regenerateData) {
      securityUsers = generationService.generateUsers(true);
    } else {
      final ClassPathResource resource = getClassPathResource("seed/example-users.json");
      if (!resource.exists()) {
        throw new RuntimeException("Unable to locate development environment users seed file");
      } else if (!resource.isReadable()) {
        throw new RuntimeException("Unable to read from development environment users seed file");
      }
      try {
        securityUsers = Arrays.stream(objectMapper.readValue(resource.getInputStream(), SecurityUser[].class)).toList();
      } catch (IOException e) {
        throw new RuntimeException("Failed to read development environment users from classpath resource " + resource.getPath(), e);
      }
    }
    return securityUsers.stream().peek(user -> {
          user.setPassword(passwordEncoder.encode(user.getPassword()));
          if (user.getAccounts() != null) {
            user.getAccounts().stream().forEach(account -> {
              if (account.getAccountTransactions() != null) {
                // this is jpa related - because we have bi-directional references we need to make sure the
                // seeded account transactions have User object set as well
                account.getAccountTransactions().stream().filter(Objects::nonNull).forEach(transaction -> {
                  transaction.setUser(user);
                });
              }
            });
          }
        })
        .toList();
  }

  /**
   * Generic method for locating and returning classpath resource which throws
   * {@link IllegalArgumentException} if the resource doesn't exist and
   * {@link IllegalStateException} if the resource cannot be read
   *
   * @param path the resource path where the file is located
   * @return the ClassPathResource object
   */
  private ClassPathResource getClassPathResource(String path) {
    // Guava has some nice stuff for reading classpath resources, I just wanted to do this manually this time
    ClassPathResource resource = new ClassPathResource(path);
    if (!resource.exists()) {
      throw new IllegalArgumentException("Unable to locate classpath resource at '" + path + "'");
    } else if (!resource.isReadable()) {
      throw new IllegalStateException("Unable to read classpath resource at '" + path + "'");
    }
    return resource;
  }
}
