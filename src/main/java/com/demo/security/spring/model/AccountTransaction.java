package com.demo.security.spring.model;

import com.demo.security.spring.utils.DecimalScaleManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "account_transactions", indexes = {
    @Index(name = "ix_account_transactions_user_id", columnList = "user_id,account_number")
})
@Getter
@Setter
@EqualsAndHashCode( exclude = { "account" })
@ToString(exclude = { "account" } )
@SequenceGenerator(name = "account_transactions_transaction_id_seq", sequenceName = "account_transactions_transaction_id_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountTransaction {

  @Id
  @Column(name = "transaction_id")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_transactions_transaction_id_seq")
  private Long transactionId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "account_number", nullable = false, updatable = false, insertable = true)
  @JsonIgnore
  @NotNull
  private Account account;

  @NotNull
  @Column(name = "user_id", nullable = false, updatable = false, insertable = true)
  private Long userId;

  @Column(name = "transaction_date", nullable = false)
  private ZonedDateTime transactionDate;

  @Column(name = "transaction_summary", length = 255, nullable = false)
  private String transactionSummary;

  @Column(name = "transaction_type", length = 100, nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionType transactionType;

  @Column(name = "transaction_amount", precision = 38, scale = 2, nullable = false)
  private BigDecimal transactionAmount;

  @Column(name = "closing_balance", precision = 38, scale = 2, nullable = false)
  private BigDecimal closingBalance;

  @Embedded
  private EntityCreatedDate createdDate = new EntityCreatedDate();

  public Long getAccountNumber() {
    return account != null ? account.getAccountNumber() : null;
  }

  @PrePersist
  protected void onCreate() {
    transactionAmount = DecimalScaleManager.setScale(transactionAmount);
    closingBalance = DecimalScaleManager.setScale(closingBalance);
  }

  @PreUpdate
  protected void onUpdate() {
    transactionAmount = DecimalScaleManager.setScale(transactionAmount);
    closingBalance = DecimalScaleManager.setScale(closingBalance);
  }
}
