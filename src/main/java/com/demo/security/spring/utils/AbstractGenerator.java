package com.demo.security.spring.utils;

import com.demo.security.spring.model.EntityControlDates;
import com.demo.security.spring.model.EntityCreatedDate;
import com.demo.security.spring.model.EntityStartAndEndDates;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Preconditions;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.datafaker.Faker;

public abstract class AbstractGenerator<T> implements Generator<T> {

  protected final Faker faker = new Faker();
  protected final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Getter
  private int itemCount = 20;

  protected ZonedDateTime randomPastDate() {
    return randomPastDate(100, TimeUnit.DAYS);
  }

  protected ZonedDateTime randomPastDate(int atMost, TimeUnit timeUnit) {
    return ZonedDateTime.ofInstant(faker.timeAndDate().past(atMost, timeUnit), ZoneId.systemDefault());
  }

  protected ZonedDateTime randomPastDate(int atMost, int min, TimeUnit timeUnit) {
    return ZonedDateTime.ofInstant(faker.timeAndDate().past(atMost, min, timeUnit), ZoneId.systemDefault());
  }

  protected ZonedDateTime randomFutureDate() {
    return ZonedDateTime.ofInstant(faker.timeAndDate().future(10, TimeUnit.DAYS), ZoneId.systemDefault());
  }

  protected ZonedDateTime randomFutureDate(int atMost, TimeUnit timeUnit) {
    return ZonedDateTime.ofInstant(faker.timeAndDate().future(atMost, timeUnit), ZoneId.systemDefault());
  }

  protected ZonedDateTime randomFutureDate(int atMost, int min, TimeUnit timeUnit) {
    return ZonedDateTime.ofInstant(faker.timeAndDate().future(atMost, min, timeUnit), ZoneId.systemDefault());
  }

  protected EntityCreatedDate randomEntityCreatedDate() {
    final EntityCreatedDate entityCreatedDate = new EntityCreatedDate();
    entityCreatedDate.setCreated(randomPastDate());
    return entityCreatedDate;
  }

  protected EntityControlDates randomEntityControlDates() {
    final EntityControlDates entityControlDates = new EntityControlDates();
    entityControlDates.setCreated(randomPastDate());
    entityControlDates.setLastUpdated(ZonedDateTime.now());
    return entityControlDates;
  }

  protected EntityStartAndEndDates randomEntityStartAndEndDates() {
    final EntityStartAndEndDates startAndEndDates = new EntityStartAndEndDates();
    startAndEndDates.setStartDate(randomPastDate());
    startAndEndDates.setEndDate(randomFutureDate());
    return startAndEndDates;
  }

  public AbstractGenerator<T> setItemCount(int itemCount) {
    Preconditions.checkArgument(itemCount > 0, "Item count for generation must be > 0");
    this.itemCount = itemCount;
    return this;
  }

}
