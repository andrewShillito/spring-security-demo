package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.PrePersist;
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
public class EntityCreatedDate {

  @Column(name = "created_date")
  private ZonedDateTime created;

  @PrePersist
  protected void onCreate() {
    created = ZonedDateTime.now();
  }
}
