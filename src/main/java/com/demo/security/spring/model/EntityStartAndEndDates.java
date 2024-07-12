package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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
public class EntityStartAndEndDates {

  @Column(name = "start_date")
  private ZonedDateTime startDate;

  @Column(name = "end_date")
  private ZonedDateTime endDate;

}
