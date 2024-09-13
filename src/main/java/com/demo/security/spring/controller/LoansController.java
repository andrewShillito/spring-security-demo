package com.demo.security.spring.controller;

import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.LoanRepository;
import com.demo.security.spring.service.UserDetailsManagerImpl;
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

  private LoanRepository loanRepository;

  private UserDetailsManagerImpl userDetailsManager;

  @Autowired
  public void setLoanRepository(LoanRepository loanRepository) {
    this.loanRepository = loanRepository;
  }

  @Autowired
  public void setUserDetailsManager(
      UserDetailsManagerImpl userDetailsManager) {
    this.userDetailsManager = userDetailsManager;
  }

  @GetMapping(RESOURCE_PATH)
  public List<Loan> getLoansDetails(Authentication authentication) throws AuthenticationException {
    SecurityUser user = userDetailsManager.getAuthenticatedUser(authentication);
    if (user != null && user.getId() != null) {
      return loanRepository.findAllByUserId(user.getId());
    }
    throw new AuthenticationException("Security user for authentication " + authentication + " could not be located or has null id!");
  }
}
