package com.demo.security.spring.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public enum TransactionType {

  Withdrawal,
  Deposit;

  static final Map<String, TransactionType> NAME_MAP = Map.of(
      Withdrawal.name(), Withdrawal,
      Deposit.name(), Deposit
  );

  static TransactionType fromString(String transactionTypeName) {
    return NAME_MAP.get(transactionTypeName);
  }
}
