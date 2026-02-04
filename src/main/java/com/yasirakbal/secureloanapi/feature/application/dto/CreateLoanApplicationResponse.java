package com.yasirakbal.secureloanapi.feature.application.dto;

import com.yasirakbal.secureloanapi.feature.loan.enums.LoanStatusType;
import com.yasirakbal.secureloanapi.feature.loan.enums.LoanType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateLoanApplicationResponse {
    private Long id;
    private LoanType loanType;
    private BigDecimal requestedAmount;
    private Integer requestedTerm;
    private BigDecimal monthlyInstallment;
    private BigDecimal interestRate;
    private BigDecimal totalPayment;
    private BigDecimal dtiRatio;
    private LoanStatusType status;
    private LocalDateTime createdAt;
}
