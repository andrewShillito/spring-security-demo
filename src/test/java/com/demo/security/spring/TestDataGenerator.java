package com.demo.security.spring;

import com.demo.security.spring.generate.AccountGenerator;
import com.demo.security.spring.generate.CardGenerator;
import com.demo.security.spring.generate.ContactMessageGenerator;
import com.demo.security.spring.generate.LoanGenerator;
import com.demo.security.spring.generate.NoticeDetailsGenerator;
import com.demo.security.spring.generate.UserGenerator;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
public class TestDataGenerator {

  @Autowired
  private Faker faker;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserGenerator userGenerator;

  @Autowired
  private AccountGenerator accountGenerator;

  @Autowired
  private LoanGenerator loanGenerator;

  @Autowired
  private CardGenerator cardGenerator;

  @Autowired
  private ContactMessageGenerator contactMessageGenerator;

  @Autowired
  private NoticeDetailsGenerator noticeDetailsGenerator;

  @Autowired
  private SecurityUserRepository securityUserRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public String randomUsername() {
    return faker.internet().username();
  }

  public String randomPassword() {
    return faker.internet().password();
  }

  public List<SecurityUser> generateUsers(int count, boolean persist, boolean internal) {
    final List<SecurityUser> users = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      if (internal) {
        users.add(generateInternalUser(faker.internet().username(), faker.internet().password(), persist));
      } else {
        users.add(generateExternalUser(faker.internet().username(), faker.internet().password(), persist));
      }
    }
    return users;
  }

  public SecurityUser generateExternalUser(@NonNull String username, @NonNull String password, boolean persist) {
    final SecurityUser user = userGenerator.generateExternalUser(username, password);
    postProcessGeneratedUser(user);
    if (persist) {
      securityUserRepository.save(user);
    }
    return user;
  }

  public SecurityUser generateInternalUser(@NonNull String username, @NonNull String password, boolean persist) {
    final SecurityUser user = userGenerator.generateInternalUser(username, password);
    postProcessGeneratedUser(user);
    if (persist) {
      securityUserRepository.save(user);
    }
    return user;
  }

  private void postProcessGeneratedUser(@NonNull SecurityUser user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
  }
}
