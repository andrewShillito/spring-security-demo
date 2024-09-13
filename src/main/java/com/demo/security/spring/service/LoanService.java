package com.demo.security.spring.service;

import com.demo.security.spring.model.Loan;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface LoanService {

  List<Loan> getLoansForUser(Authentication authentication);

  List<Loan> getLoansForUser();

}
