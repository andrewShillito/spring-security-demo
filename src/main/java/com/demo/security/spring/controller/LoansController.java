package com.demo.security.spring.controller;

import com.demo.security.spring.model.Loan;
import com.demo.security.spring.service.LoanService;
import java.util.List;
import javax.naming.AuthenticationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class LoansController {

  public static final String RESOURCE_PATH = "/myLoans";

  private LoanService loanService;

  @Autowired
  public void setLoanService(LoanService loanService) {
    this.loanService = loanService;
  }

  @GetMapping(RESOURCE_PATH)
  public List<Loan> getLoansDetails(Authentication authentication) throws AuthenticationException {
    return loanService.getLoansForUser(authentication);
  }
}
