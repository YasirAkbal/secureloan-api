package com.yasirakbal.secureloanapi.feature.application.repository;

import com.yasirakbal.secureloanapi.feature.loan.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<Loan, Long> {
}
