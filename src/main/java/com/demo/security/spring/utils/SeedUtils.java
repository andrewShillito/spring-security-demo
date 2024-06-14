package com.demo.security.spring.utils;

import com.demo.security.spring.model.SecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

@Log4j2
public class SeedUtils {

  /**
   * Returns a list of UserDetails from a csv resource. This method of user seeding is unsafe for
   * anything other than local demo/sample-apps. Note that the expected format of the seed file is
   * csv with each user separated by a line. Example: userName,password,authority1
   * userName2,password2,authority1|authority2|authority3 userName3,password3,authority1|authority2
   *
   * @return list of user details object to be seeded into in-memory user details manager
   */
  public static List<UserDetails> getInMemoryUsers(final PasswordEncoder passwordEncoder) {
    ClassPathResource resource = getClassPathResource("seed/in-memory-users.csv");
    try (BufferedReader in = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
      List<UserDetails> users = new ArrayList<>();
      String line;
      while (in.ready() && (line = in.readLine()) != null) {
        final String[] cols = line.split(",");
        if (cols.length != 3) {
          throw new IllegalArgumentException(
              "Expected csv file in-memory user to have 3 columns but had " + cols.length
                  + " for line: " + line);
        }
        final List<GrantedAuthority> authorities = readAuthoritiesPipeSeparated(cols[2]);
        if (authorities.isEmpty()) {
          final String finalLine = line;
          log.warn(() -> "User from line " + finalLine + " has no authorities!");
        }
        // unsafe and only for sample local app
        users.add(User
            .withUsername(cols[0].trim())
            .password(passwordEncoder.encode(cols[1].trim()))
            .authorities(authorities).build()
        );
      }
      if (!users.isEmpty()) {
        return users;
      } else {
        throw new IllegalStateException(
            "No users were loaded for the in memory user details manager");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Reads a json file for users to seed into the database.
   * Because hibernate and data.sql don't always get along and this simplifies the handling of
   * user seeding by allowing the app to check if users exist before we try to seed them again.
   * Should not be used in any production environment.
   * @return a List of security users to populate into the database
   */
  public static List<SecurityUser> getDevEnvironmentDbUsers() {
    final ClassPathResource resource = getClassPathResource("seed/db-users.json");
    if (!resource.exists()) {
      throw new RuntimeException("Unable to locate development environment users seed file");
    } else if (!resource.isReadable()) {
      throw new RuntimeException("Unable to read from development environment users seed file");
    }
    final ObjectMapper objectMapper = new ObjectMapper();
    try {
      SecurityUser[] users = objectMapper.readValue(resource.getInputStream(), SecurityUser[].class);
      return Arrays.asList(users);
    } catch (IOException e) {
      throw new RuntimeException("Failed to populate development environment users into the database");
    }
  }

  /**
   * Generic method for locating and returning classpath resource which throws
   * {@link IllegalArgumentException} if the resource doesn't exist and
   * {@link IllegalStateException} if the resource cannot be read
   *
   * @param path the resource path where the file is located
   * @return the ClassPathResource object
   */
  public static ClassPathResource getClassPathResource(String path) {
    // Guava has some nice stuff for reading classpath resources, I just wanted to do this manually this time
    ClassPathResource resource = new ClassPathResource(path);
    if (!resource.exists()) {
      throw new IllegalArgumentException("Unable to locate classpath resource at '" + path + "'");
    } else if (!resource.isReadable()) {
      throw new IllegalStateException("Unable to read classpath resource at '" + path + "'");
    }
    return resource;
  }

  /**
   * Reads a pipe '|' separated string and turns it into a List of simple granted authority objects
   *
   * @param pipeSeparatedAuthorities a pipe separated string of authorities - note that ROLE_ must
   *                                 not be prefixed
   * @return located granted authorities for the users
   */
  private static List<GrantedAuthority> readAuthoritiesPipeSeparated(
      String pipeSeparatedAuthorities) {
    final List<GrantedAuthority> result = new ArrayList<>();
    if (StringUtils.isNotBlank(pipeSeparatedAuthorities)) {
      final String[] split = pipeSeparatedAuthorities.split("\\|");
      if (split.length >= 1) {
        for (String role : split) {
          result.add(new SimpleGrantedAuthority(role.trim()));
        }
      } else {
        log.error(() -> "Expected user to have at least one role but found none from raw string: "
            + pipeSeparatedAuthorities);
      }
    }
    return result;
  }
}
