package com.demo.security.spring.controller;

import com.demo.security.spring.model.Loan;
import com.demo.security.spring.repository.LoanRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoansController {

  public static final String LOANS_RESOURCE_PATH = "/myLoans";

  private LoanRepository loanRepository;

  @Autowired
  public void setLoanRepository(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @GetMapping(LOANS_RESOURCE_PATH)
  public List<Loan> getLoansDetails(@RequestParam long userId) {
    return loanRepository.findAllByUserId(userId);
  }
}
