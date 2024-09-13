package com.demo.security.spring.service;

import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.LoanRepository;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;

@Log4j2
@Builder
public class LoanServiceImpl extends AbstractUserAwareService implements LoanService {

  private LoanRepository loanRepository;

  private final Function<SecurityUser, List<Loan>> getAllForUser = u -> loanRepository.findAllByUserId(u.getId());

  @Override
  public List<Loan> getLoansForUser(Authentication authentication) {
    return executeForUser(authentication, getAllForUser);
  }

  @Override
  public List<Loan> getLoansForUser() {
    return executeForUser(getAllForUser);
  }
}
