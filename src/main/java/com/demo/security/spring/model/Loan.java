package com.demo.security.spring.model;

import com.demo.security.spring.utils.DecimalScaleManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "loans")
@Getter
@Setter
@EqualsAndHashCode
@ToString(exclude = { "user" })
@SequenceGenerator(name = "loans_loan_number_seq", sequenceName = "loans_loan_number_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
public class Loan {

  @Id
  @Column(name = "loan_number")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loans_loan_number_seq")
  private Long loanNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, updatable = false, insertable = true)
  @JsonIgnore
  @NotNull
  private SecurityUser user;

  // TODO: create loan types enum
  @Column(name = "loan_type", length = 100)
  private String loanType;

  @Column(name = "start_date")
  private ZonedDateTime startDate;

  @Column(name = "total_amount")
  private BigDecimal totalAmount;

  @Column(name = "amount_paid")
  private BigDecimal amountPaid;

  @Column(name = "outstanding_amount")
  private BigDecimal outstandingAmount;

  @Embedded
  private EntityCreatedDate createdDate = new EntityCreatedDate();

  @PrePersist
  protected void onCreate() {
    totalAmount = DecimalScaleManager.setScale(totalAmount);
    amountPaid = DecimalScaleManager.setScale(amountPaid);
    outstandingAmount = DecimalScaleManager.setScale(outstandingAmount);
  }

  @PreUpdate
  protected void onUpdate() {
    totalAmount = DecimalScaleManager.setScale(totalAmount);
    amountPaid = DecimalScaleManager.setScale(amountPaid);
    outstandingAmount = DecimalScaleManager.setScale(outstandingAmount);
  }
}
