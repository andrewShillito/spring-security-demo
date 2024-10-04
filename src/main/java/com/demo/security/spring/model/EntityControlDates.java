package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
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
@JsonInclude(Include.NON_EMPTY)
public class EntityControlDates {

  @Column(name = "created_date", nullable = false)
  private ZonedDateTime created;

  @Column(name = "last_updated_date", nullable = false)
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
