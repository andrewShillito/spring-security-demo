package com.demo.security.spring.service;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.ContactMessage;
import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.NoticeDetails;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.utils.ValidationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;

/**
 * Handles retrieving lists of example users and other data from resource files for local dev environment testing.
 */
@Log4j2
@Builder
@Setter
public class ExampleDataManager {

  private ObjectMapper objectMapper;

  private ExampleDataGenerationService generationService;

  private boolean regenerateData;

  public static final String USERS_OUTPUT_FILE_NAME = "example-users.json";
  public static final String ACCOUNTS_OUTPUT_FILE_NAME = "example-accounts.json";
  public static final String LOANS_OUTPUT_FILE_NAME = "example-loans.json";
  public static final String CARDS_OUTPUT_FILE_NAME = "example-cards.json";
  public static final String NOTICES_OUTPUT_FILE_NAME = "example-notice-details.json";
  public static final String CONTACT_MESSAGES_OUTPUT_FILE_NAME = "example-contact-messages.json";

  /**
   * Reads a json file for users to seed into the database.
   * Because hibernate and data.sql don't always get along and this simplifies the handling of
   * user seeding by allowing the app to check if users exist before we try to seed them again.
   * Should not be used in any production environment.
   * @return a List of security users to populate into the database
   */
  public List<SecurityUser> getUsers() {
    List<SecurityUser> securityUsers;
    if (regenerateData) {
      securityUsers = generationService.generateUsers();
    } else {
      final ClassPathResource resource = getClassPathResource("seed/" + USERS_OUTPUT_FILE_NAME);
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
    return securityUsers;
  }

  public List<Account> getAccountsForUsers(List<SecurityUser> users) {
    ValidationUtils.notEmpty(users);
    List<Account> accounts;
    if (regenerateData) {
      accounts = generationService.generateAccounts(users);
    } else {
      final ClassPathResource resource = getClassPathResource("seed/" + ACCOUNTS_OUTPUT_FILE_NAME);
      if (!resource.exists()) {
        throw new RuntimeException("Unable to locate development environment accounts seed file");
      } else if (!resource.isReadable()) {
        throw new RuntimeException("Unable to read from development environment accounts seed file");
      }
      try {
        accounts = Arrays.stream(objectMapper.readValue(resource.getInputStream(), Account[].class)).toList();
      } catch (IOException e) {
        throw new RuntimeException("Failed to read development environment accounts from classpath resource " + resource.getPath(), e);
      }
    }
    return accounts;
  }

  public List<Card> getCardsForUsers(List<SecurityUser> users) {
    ValidationUtils.notEmpty(users);
    List<Card> cards;
    if (regenerateData) {
      cards = generationService.generateCards(users);
    } else {
      final ClassPathResource resource = getClassPathResource("seed/" + CARDS_OUTPUT_FILE_NAME);
      if (!resource.exists()) {
        throw new RuntimeException("Unable to locate development environment cards seed file");
      } else if (!resource.isReadable()) {
        throw new RuntimeException("Unable to read from development environment cards seed file");
      }
      try {
        cards = Arrays.stream(objectMapper.readValue(resource.getInputStream(), Card[].class)).toList();
      } catch (IOException e) {
        throw new RuntimeException("Failed to read development environment accounts from classpath resource " + resource.getPath(), e);
      }
    }
    return cards;
  }

  public List<Loan> getLoansForUsers(List<SecurityUser> users) {
    ValidationUtils.notEmpty(users);
    List<Loan> loans;
    if (regenerateData) {
      loans = generationService.generateLoans(users);
    } else {
      final ClassPathResource resource = getClassPathResource("seed/" + LOANS_OUTPUT_FILE_NAME);
      if (!resource.exists()) {
        throw new RuntimeException("Unable to locate development environment loans seed file");
      } else if (!resource.isReadable()) {
        throw new RuntimeException("Unable to read from development environment loans seed file");
      }
      try {
        loans = Arrays.stream(objectMapper.readValue(resource.getInputStream(), Loan[].class)).toList();
      } catch (IOException e) {
        throw new RuntimeException("Failed to read development environment accounts from classpath resource " + resource.getPath(), e);
      }
    }
    return loans;
  }

  public List<NoticeDetails> getNoticeDetails() {
    List<NoticeDetails> noticeDetails;
    if (regenerateData) {
      noticeDetails = generationService.generateNotices();
    } else {
      final ClassPathResource resource = getClassPathResource("seed/" + NOTICES_OUTPUT_FILE_NAME);
      try {
        noticeDetails = Arrays.stream(objectMapper.readValue(resource.getInputStream(), NoticeDetails[].class)).toList();
      } catch (IOException e) {
        throw new RuntimeException("Failed to read development environment notice details from classpath resource " + resource.getPath(), e);
      }
    }
    return noticeDetails;
  }

  public List<ContactMessage> getContactMessages() {
    List<ContactMessage> contactMessages;
    if (regenerateData) {
      contactMessages = generationService.generateMessages();
    } else {
      final ClassPathResource resource = getClassPathResource("seed/" + CONTACT_MESSAGES_OUTPUT_FILE_NAME);
      try {
        contactMessages = Arrays.stream(objectMapper.readValue(resource.getInputStream(), ContactMessage[].class)).toList();
      } catch (IOException e) {
        throw new RuntimeException("Failed to read development environment contact messages from classpath resource " + resource.getPath(), e);
      }
    }
    return contactMessages;
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
