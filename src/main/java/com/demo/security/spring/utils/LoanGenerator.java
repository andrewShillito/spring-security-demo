package com.demo.security.spring.utils;

import com.demo.security.spring.model.Loan;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LoanGenerator extends AbstractGenerator<List<Loan>> {

  @Override
  public List<Loan> generate() {
    final List<Loan> loans = new ArrayList<>();
    for (int i = 0; i < getItemCount(); i++) {
      loans.add(generateLoan());
    }
    log.info(() -> "Generated " + loans.size() + " loans");
    return loans;
  }

  private Loan generateLoan() {
    final Loan loan = new Loan();
    loan.setLoanType("Mortgage");
    loan.setCreatedDate(randomEntityCreatedDate());
    loan.setTotalAmount(BigDecimal.valueOf(faker.random().nextDouble(10000, 500000)).setScale(2, RoundingMode.HALF_EVEN));
    loan.setAmountPaid(BigDecimal.valueOf(faker.random().nextDouble(0, loan.getTotalAmount().doubleValue())).setScale(2, RoundingMode.HALF_EVEN));
    loan.setOutstandingAmount(loan.getTotalAmount().subtract(loan.getAmountPaid()).setScale(2, RoundingMode.HALF_EVEN));
    loan.setStartDate(randomPastDate());
    return loan;
  }
}
