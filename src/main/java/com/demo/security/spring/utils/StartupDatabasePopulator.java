package com.demo.security.spring.utils;

import com.demo.security.spring.model.ContactMessage;
import com.demo.security.spring.model.NoticeDetails;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.ContactMessageRepository;
import com.demo.security.spring.repository.NoticeDetailsRepository;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.service.ExampleDataManager;
import java.util.List;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
@Log4j2
public class StartupDatabasePopulator {

  private ExampleDataManager exampleDataManager;

  private SecurityUserRepository securityUserRepository;

  private NoticeDetailsRepository noticeDetailsRepository;

  private ContactMessageRepository contactMessageRepository;

  private PasswordEncoder passwordEncoder;

  @EventListener(ContextRefreshedEvent.class)
  public void seedDatabaseIfEmpty() {
    try {
      populateUsers();
      populateNoticeDetails();
      populateContactMessages();
    } catch (Exception e) {
      throw new RuntimeException("Failed to populate development environment with error!", e);
    }
  }

  private void populateUsers() {
    if (securityUserRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment users as the table already contains data");
    } else {
      log.info(() -> "Populating development environment security users");
      final List<SecurityUser> users = exampleDataManager.getUsers();
      securityUserRepository.saveAll(users);
    }
  }

  private void populateNoticeDetails() {
    if (noticeDetailsRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment notice details as the table already contains data");
    } else {
      log.info(() -> "Populating development environment notice details");
      final List<NoticeDetails> noticeDetails = exampleDataManager.getNoticeDetails();
      noticeDetailsRepository.saveAll(noticeDetails);
      log.info(() -> "Finished populating " + noticeDetails.size() + " development environment notice details");
    }
  }

  private void populateContactMessages() {
    if (contactMessageRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment contact messages as the table already contains data");
    } else {
      log.info(() -> "Populating development environment contact messages");
      final List<ContactMessage> contactMessages = exampleDataManager.getContactMessages();
      contactMessageRepository.saveAll(contactMessages);
      log.info(() -> "Finished populating " + contactMessages.size() + " development environment contact messages");
    }
  }

}
