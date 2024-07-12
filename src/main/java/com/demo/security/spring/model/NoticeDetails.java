package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "notice_details")
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

  @Column(name = "notice_summary")
  private String noticeSummary;

  @Column(name = "notice_details")
  private String noticeDetails;

  @Embedded
  private EntityControlDates controlDates;

  @Embedded
  private EntityStartAndEndDates entityStartAndEndDates;

}
