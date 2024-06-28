package com.demo.security.spring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class EntityStartAndEndDates {

  @Column(name = "start_date")
  private ZonedDateTime start_date;

  @Column(name = "end_date")
  private ZonedDateTime end_date;

  @Column(name = "created_date")
  private ZonedDateTime created;

  @Column(name = "last_updated_date")
  private ZonedDateTime lastUpdated;

  @PrePersist
  protected void onCreate() {
    created = ZonedDateTime.now();
    lastUpdated = ZonedDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    lastUpdated = ZonedDateTime.now();
  }
}
