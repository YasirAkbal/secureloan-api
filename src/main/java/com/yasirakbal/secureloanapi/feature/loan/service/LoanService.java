package com.yasirakbal.secureloanapi.feature.loan.service;

import com.yasirakbal.secureloanapi.feature.loan.repository.LoanRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoanService {
    private LoanRepository loanRepository;
}
