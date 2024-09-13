package com.demo.security.spring.service;

import com.demo.security.spring.model.Loan;
import com.demo.security.spring.model.SecurityUser;
import com.demo.security.spring.repository.LoanRepository;
import java.util.List;
import java.util.function.Function;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Log4j2
@Builder
public class LoanServiceImpl extends AbstractUserAwareService implements LoanService {

  private LoanRepository loanRepository;

  @Override
  public List<Loan> getLoansForUser(Authentication authentication) {
    final Function<SecurityUser, List<Loan>> function = u -> loanRepository.findAllByUserId(u.getId());
    return executeForUser(authentication, function);
  }
}
