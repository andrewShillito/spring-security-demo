package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "ix_accounts_user_id", columnList = "user_id,account_number", unique = true)
})
@SequenceGenerator(name = "accounts_account_number_seq", sequenceName = "accounts_account_number_seq", allocationSize = 50, initialValue = 1)
@Getter
@Setter
@EqualsAndHashCode(exclude = { "accountTransactions" })
@ToString
@JsonInclude(Include.NON_EMPTY)
public class Account {

  @Id
  @Column(name = "account_number")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounts_account_number_seq")
  private Long accountNumber;

  @NotNull
  @Column(name = "user_id", nullable = false, updatable = false, insertable = true)
  private Long userId;

  @Column(name = "account_type", length = 100, nullable = false)
  private String accountType;

  // TODO: This is a free-text address, but Address object would be preferable
  @Column(name = "branch_address", length = 255, nullable = false)
  private String branchAddress;

  @Embedded
  private EntityCreatedDate createdDate = new EntityCreatedDate();

  @OneToMany(mappedBy = "account", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AccountTransaction> accountTransactions;

  public void setAccountTransactions(List<AccountTransaction> accountTransactions) {
    this.accountTransactions = accountTransactions;
    if (accountTransactions != null) {
      accountTransactions.stream().filter(Objects::nonNull).forEach(transaction -> {
        transaction.setAccount(this);
        transaction.setUserId(this.userId);
      });
    }
  }
}
