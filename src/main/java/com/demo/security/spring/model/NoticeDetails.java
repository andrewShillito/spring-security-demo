package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "notice_details", indexes = {
    @Index(name = "ix_notice_details_start_date_end_date", columnList = "start_date,end_date")
})
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator(name = "notice_details_notice_id_seq", sequenceName = "notice_details_notice_id_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
public class NoticeDetails {

  @Id
  @Column(name = "notice_id")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice_details_notice_id_seq")
  private Long noticeId;

  @Column(name = "notice_summary", length = 255, nullable = false)
  @NotBlank
  private String noticeSummary;

  @Column(name = "notice_details", length = 500, nullable = false)
  @NotBlank
  private String noticeDetails;

  @Embedded
  private EntityControlDates controlDates;

  @Embedded
  private EntityStartAndEndDates entityStartAndEndDates;

  @JsonIgnore
  public boolean isFuture() {
    return startAndEndAreValid() && entityStartAndEndDates.getStartDate().isAfter(ZonedDateTime.now());
  }

  @JsonIgnore
  public boolean isPast() {
    return startAndEndAreValid() && entityStartAndEndDates.getEndDate().isBefore(ZonedDateTime.now());
  }

  @JsonIgnore
  public boolean isActive() {
    return startAndEndAreValid()
        && entityStartAndEndDates.getStartDate().isBefore(ZonedDateTime.now())
        && entityStartAndEndDates.getEndDate().isAfter(ZonedDateTime.now());
  }

  private boolean startAndEndAreValid() {
    return entityStartAndEndDates != null
        && entityStartAndEndDates.getStartDate() != null
        && entityStartAndEndDates.getEndDate() != null
        // should assert this at other times as well - this would be bad data
        && entityStartAndEndDates.getEndDate().isAfter(entityStartAndEndDates.getStartDate());
  }
}
