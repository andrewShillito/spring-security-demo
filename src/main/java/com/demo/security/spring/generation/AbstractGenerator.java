package com.demo.security.spring.generation;

import com.demo.security.spring.model.EntityControlDates;
import com.demo.security.spring.model.EntityCreatedDate;
import com.demo.security.spring.model.EntityStartAndEndDates;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import net.datafaker.Faker;

@Getter
public abstract class AbstractGenerator<T> implements Generator<T> {

  protected final Faker faker;

  protected final ObjectMapper objectMapper;

  public static final int DEFAULT_ITEM_COUNT = 20;

  @Setter
  private int itemCount;

  public AbstractGenerator(Faker faker, ObjectMapper objectMapper) {
    this.faker = faker;
    this.objectMapper = objectMapper;
    this.itemCount = DEFAULT_ITEM_COUNT;
  }

  public AbstractGenerator(Faker faker, ObjectMapper objectMapper, int itemCount) {
    this.faker = faker;
    this.objectMapper = objectMapper;
    this.itemCount = itemCount;
  }

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
}
