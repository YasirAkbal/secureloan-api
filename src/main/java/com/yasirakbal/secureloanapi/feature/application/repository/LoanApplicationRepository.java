package com.yasirakbal.secureloanapi.feature.application.repository;

import com.yasirakbal.secureloanapi.feature.application.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
}
