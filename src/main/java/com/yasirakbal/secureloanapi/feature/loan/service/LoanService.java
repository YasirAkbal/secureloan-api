package com.yasirakbal.secureloanapi.feature.loan.service;

import com.yasirakbal.secureloanapi.feature.loan.entity.Loan;
import com.yasirakbal.secureloanapi.feature.loan.repository.LoanRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class LoanService {
    private LoanRepository loanRepository;

    @Transactional
    public Loan createLoan(Loan loanToCreate) {
        return loanRepository.save(loanToCreate);
    }
}
