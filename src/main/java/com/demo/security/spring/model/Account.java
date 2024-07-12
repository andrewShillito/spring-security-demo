package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "accounts")
@SequenceGenerator(name = "accounts_account_number_seq", sequenceName = "accounts_account_number_seq", allocationSize = 50, initialValue = 1)
@Getter
@Setter
@EqualsAndHashCode
@ToString(exclude = "user")
@JsonInclude(Include.NON_EMPTY)
public class Account {

  @Id
  @Column(name = "account_number")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounts_account_number_seq")
  private Long accountNumber;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false, updatable = false, insertable = true)
  @JsonIgnore
  @NotNull
  private SecurityUser user;

  @Column(name = "account_type")
  private String accountType;

  // TODO: This is a free-text address, but Address object would be preferable
  @Column(name = "branch_address")
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
        transaction.setUser(this.getUser());
      });
    }
  }
}
