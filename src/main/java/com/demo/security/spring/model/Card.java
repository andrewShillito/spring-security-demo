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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cards")
@Getter
@Setter
@EqualsAndHashCode
@ToString(exclude = { "user" } )
@SequenceGenerator(name = "cards_card_id_seq", sequenceName = "cards_card_id_seq", allocationSize = 50, initialValue = 1)
@JsonInclude(Include.NON_EMPTY)
public class Card {

  @Id
  @Column(name = "card_id")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cards_card_id_seq")
  private Long id;

  @Column(name = "card_number")
  private String cardNumber;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, updatable = false, insertable = true)
  @JsonIgnore
  @NotNull
  private SecurityUser user;

  @Column(name = "card_type")
  private String cardType;

  @Column(name = "total_limit")
  private BigDecimal totalLimit;

  @Column(name = "amount_used")
  private BigDecimal amountUsed;

  @Column(name = "available_amount")
  private BigDecimal availableAmount;

  @Embedded
  private EntityCreatedDate createdDate;

  @PrePersist
  protected void onCreate() {
    totalLimit = DecimalScaleManager.setScale(totalLimit);
    amountUsed = DecimalScaleManager.setScale(amountUsed);
    availableAmount = DecimalScaleManager.setScale(availableAmount);
  }

  @PreUpdate
  protected void onUpdate() {
    totalLimit = DecimalScaleManager.setScale(totalLimit);
    amountUsed = DecimalScaleManager.setScale(amountUsed);
    availableAmount = DecimalScaleManager.setScale(availableAmount);
  }
}
