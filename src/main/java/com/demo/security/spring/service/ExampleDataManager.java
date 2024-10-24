package com.demo.security.spring.service;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.Card;
import com.demo.security.spring.model.ContactMessage;
import com.demo.security.spring.model.ExampleSecurityGroupDataWrapper;
import com.demo.security.spring.model.SecurityGroup;
import com.demo.security.spring.model.SecurityGroupConfig;
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

  public static final String GROUPS_OUTPUT_FILE_NAME = "example-authorities.json";
  public static final String GROUPS_CONFIG_OUTPUT_FILE_NAME = "example-authorities-config.json";
  public static final String USERS_OUTPUT_FILE_NAME = "example-users.json";
  public static final String ACCOUNTS_OUTPUT_FILE_NAME = "example-accounts.json";
  public static final String LOANS_OUTPUT_FILE_NAME = "example-loans.json";
  public static final String CARDS_OUTPUT_FILE_NAME = "example-cards.json";
  public static final String NOTICES_OUTPUT_FILE_NAME = "example-notice-details.json";
  public static final String CONTACT_MESSAGES_OUTPUT_FILE_NAME = "example-contact-messages.json";

  public ExampleSecurityGroupDataWrapper getAuthorityGroups() {
    ExampleSecurityGroupDataWrapper result = new ExampleSecurityGroupDataWrapper();
    if (regenerateData) {
      result.setGroupConfigs(generationService.generateSecurityGroups());
    } else {
      result.setGroups(Arrays.stream(getClassPathResource("seed/" + GROUPS_OUTPUT_FILE_NAME, SecurityGroup[].class)).toList());
    }
    return result;
  }

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
      securityUsers = Arrays.stream(getClassPathResource("seed/" + USERS_OUTPUT_FILE_NAME, SecurityUser[].class)).toList();
    }
    return securityUsers;
  }

  public List<Account> getAccountsForUsers(List<SecurityUser> users) {
    ValidationUtils.notEmpty(users);
    List<Account> accounts;
    if (regenerateData) {
      accounts = generationService.generateAccounts(users);
    } else {
      accounts = Arrays.stream(getClassPathResource("seed/" + ACCOUNTS_OUTPUT_FILE_NAME, Account[].class)).toList();
    }
    return accounts;
  }

  public List<Card> getCardsForUsers(List<SecurityUser> users) {
    ValidationUtils.notEmpty(users);
    List<Card> cards;
    if (regenerateData) {
      cards = generationService.generateCards(users);
    } else {
      cards = Arrays.stream(getClassPathResource("seed/" + CARDS_OUTPUT_FILE_NAME, Card[].class)).toList();
    }
    return cards;
  }

  public List<Loan> getLoansForUsers(List<SecurityUser> users) {
    ValidationUtils.notEmpty(users);
    List<Loan> loans;
    if (regenerateData) {
      loans = generationService.generateLoans(users);
    } else {
      loans = Arrays.stream(getClassPathResource("seed/" + LOANS_OUTPUT_FILE_NAME, Loan[].class)).toList();
    }
    return loans;
  }

  public List<NoticeDetails> getNoticeDetails() {
    List<NoticeDetails> noticeDetails;
    if (regenerateData) {
      noticeDetails = generationService.generateNotices();
    } else {
      noticeDetails = Arrays.stream(getClassPathResource("seed/" + NOTICES_OUTPUT_FILE_NAME, NoticeDetails[].class)).toList();
    }
    return noticeDetails;
  }

  public List<ContactMessage> getContactMessages() {
    List<ContactMessage> contactMessages;
    if (regenerateData) {
      contactMessages = generationService.generateMessages();
    } else {
      contactMessages = Arrays.stream(getClassPathResource("seed/" + CONTACT_MESSAGES_OUTPUT_FILE_NAME, ContactMessage[].class)).toList();
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

  /**
   * Return the content of a given classpath resource mapped to the requested class.
   * Will throw if resource does not exist, cannot be read, or if mapping fails
   * @param path the path to the classpath resource
   * @param clazz the class to return
   * @return mapped content as type T from the given classpath resource or throws RuntimeException
   * @param <T> the type to return
   */
  private <T> T getClassPathResource(String path, Class<T> clazz) {
    ClassPathResource resource = getClassPathResource(path);
    try {
      return objectMapper.readValue(resource.getInputStream(), clazz);
    } catch (IOException e) {
      throw new RuntimeException("Failed to read classpath resource " + resource.getPath() + " as class " + clazz, e);
    }
  }
}
