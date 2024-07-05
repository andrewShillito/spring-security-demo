package com.demo.security.spring.utils;

import com.demo.security.spring.model.Account;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.SecurityUserRepository;
import com.demo.security.spring.service.DevEnvironmentExampleDataManager;
import com.github.javafaker.Faker;
import java.util.List;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.checkerframework.checker.units.qual.A;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

@Builder
@Log4j2
public class DevEnvironmentDbPopulator {

  /*
  * TODO:
  *  - see https://github.com/eazybytes/springsecurity6/blob/3.2.0/section6/springsecsection6/src/main/resources/sql/scripts.sql
  *    for some example insert statements to change into jpa config / population
  *  - update/create jpa entities
  *  - make jpa repositories & queries as needed
  *  - finish this class
  *  - note that this class is only loaded for postgres profile right now ( so does not run in test h2 db )
  */

  private DevEnvironmentExampleDataManager exampleDataManager;

  private SecurityUserRepository securityUserRepository;

  private PasswordEncoder passwordEncoder;

  private final Faker faker = Faker.instance();

  @EventListener(ContextRefreshedEvent.class)
  public void seedDatabaseIfEmpty() {
    populateUsers();
  }

  private void populateUsers() {
    if (securityUserRepository.count() > 0) {
      log.info(() -> "Not repopulating development environment users as the table already contains data");
    } else {
      log.info(() -> "Populating development environment security users.");
      List<SecurityUser> users = exampleDataManager.getDevEnvironmentUsers();
      users.forEach(this::addAccount);
      try {
        securityUserRepository.saveAll(users);
        log.info(() -> "Finished populating " + users.size() + " development environment users");
      } catch (Exception e) {
        throw new RuntimeException("Failed to populate development environment security users with error!", e);
      }
    }
  }

  private void addAccount(SecurityUser user) {
    Account account = new Account();
    account.setUser(user);
    account.setAccountType("checking");
    account.setBranchAddress(faker.address().fullAddress());
  }

  private void populateAccountTransactions() {

  }

  private void populateLoans() {

  }

  private void populateCards() {

  }

  private void populateNoticeDetails() {

  }

  private void populateContactMessages() {

  }

}
