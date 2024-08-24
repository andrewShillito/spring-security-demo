package com.demo.security.spring.generate;

import com.demo.security.spring.model.Loan;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import net.datafaker.Faker;

@Log4j2
public class LoanFileGenerator extends AbstractFileGenerator {

  public static final String DEFAULT_OUTPUT_FILE = "example-loans.json";

  public LoanFileGenerator(Faker faker,
      ObjectMapper objectMapper) {
    super(faker, objectMapper, DEFAULT_OUTPUT_FILE);
  }

  @Override
  public List<Loan> generate() {
    return generate(getItemCount());
  }

  @Override
  public List<Loan> generate(int count) {
    final List<Loan> loans = new ArrayList<>();
    for (int i = 0; i < count; i++) {
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
    loan.setStartDate(randomPastDate(loan.getCreatedDate().getCreated()));
    return loan;
  }
}
