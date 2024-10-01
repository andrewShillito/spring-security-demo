package com.demo.security.spring.model;

import com.demo.security.spring.utils.DecimalScaleManager;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "loans", indexes = {
    @Index(name = "ix_loans_user_id_loan_type_loan_number", columnList = "user_id,loan_type,loan_number", unique = true)
})
@Getter
@Setter
@EqualsAndHashCode
@ToString
@SequenceGenerator(name = "loans_loan_number_seq", sequenceName = "loans_loan_number_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
public class Loan {

  @Id
  @Column(name = "loan_number")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loans_loan_number_seq")
  private Long loanNumber;

  @NotNull
  @Column(name = "user_id", nullable = false, updatable = false, insertable = true)
  private Long userId;

  // TODO: create loan types enum
  @Column(name = "loan_type", length = 100, nullable = false)
  private String loanType;

  @Column(name = "start_date", nullable = false)
  private ZonedDateTime startDate;

  @Column(name = "total_amount", precision = 38, scale = 2, nullable = false)
  private BigDecimal totalAmount;

  @Column(name = "amount_paid", precision = 38, scale = 2, nullable = false)
  private BigDecimal amountPaid;

  @Column(name = "outstanding_amount", precision = 38, scale = 2, nullable = false)
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
