package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator(name = "contact_messages_contact_id_seq", sequenceName = "contact_messages_contact_id_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
public class ContactMessage {

  @Id
  @Column(name = "contact_id")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_messages_contact_id_seq")
  private Long contactId;

  @NotEmpty
  @Column(name = "contact_name")
  private String contactName;

  @NotEmpty
  @Column(name = "contact_email")
  private String contactEmail;

  @NotEmpty
  @Column(name = "subject")
  private String subject;

  @NotEmpty
  @Column(name = "message")
  private String message;

  @Column(name = "created_date")
  private ZonedDateTime created;

  @PrePersist
  protected void onCreate() {
    created = ZonedDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    if (created == null) {
      created = ZonedDateTime.now();
    }
  }

}
