package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "accounts")
@SequenceGenerator(name = "accounts_id_seq", sequenceName = "accounts_id_seq", allocationSize = 50)
@Getter
@Setter
@EqualsAndHashCode
@ToString(exclude = "user")
public class Account {

  @Id
  @Column(name = "account_number")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accounts_id_seq")
  private Long accountNumber;

  @OneToOne
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

}
