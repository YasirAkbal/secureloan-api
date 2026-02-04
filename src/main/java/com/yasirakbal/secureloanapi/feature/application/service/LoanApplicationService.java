package com.yasirakbal.secureloanapi.feature.application.service;

import com.yasirakbal.secureloanapi.common.exception.BusinessException;
import com.yasirakbal.secureloanapi.common.utils.DateUtils;
import com.yasirakbal.secureloanapi.feature.application.dto.CreateLoanApplicationRequest;
import com.yasirakbal.secureloanapi.feature.application.exception.CustomerAgeRequestedTermNotEligibleException;
import com.yasirakbal.secureloanapi.feature.application.exception.DtiNotEligibleException;
import com.yasirakbal.secureloanapi.feature.application.exception.MonthlyIncomeNotEnoughException;
import com.yasirakbal.secureloanapi.feature.application.repository.LoanApplicationRepository;
import com.yasirakbal.secureloanapi.feature.loan.entity.Loan;
import com.yasirakbal.secureloanapi.feature.loan.enums.LoanStatusType;
import com.yasirakbal.secureloanapi.feature.loan.enums.LoanType;
import com.yasirakbal.secureloanapi.feature.loan.repository.LoanRepository;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import com.yasirakbal.secureloanapi.feature.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class LoanApplicationService {
    private LoanApplicationRepository loanApplicationRepository;
    private LoanRepository loanRepository;
    private UserService userService;

    public Loan createApplication(CreateLoanApplicationRequest request, Long customerId) {
        User customer = userService.getUserById(customerId);
        LoanType requestedLoanType = request.getLoanType();
        List<BusinessException> creationErrors = new ArrayList<>();

        if(!requestedLoanType.checkIfItsEligible(customer.getMonthlyIncome())) {
            creationErrors.add(new MonthlyIncomeNotEnoughException());
        }

        LocalDate dateWhenCreditWillBeFinished = LocalDate.now().plusMonths(request.getRequestedTerm());
        int customerAgeAfterCreditFinished = Period.between(customer.getBirthDate(), dateWhenCreditWillBeFinished).getYears();
        if(customerAgeAfterCreditFinished > 65) {
            creationErrors.add(new CustomerAgeRequestedTermNotEligibleException(65, customerAgeAfterCreditFinished));
        }

        if(!checkDTI(customerId, customer.getMonthlyIncome(),
                request.getRequestedAmount(), request.getRequestedTerm()))
        {
            creationErrors.add(new DtiNotEligibleException());
        }

        if(!creationErrors.isEmpty()) {
            //rejection case
            return null;
        }

        BigDecimal monthlyInstallment = calculateMonthlyInstallment(request.getRequestedAmount(),
                request.getLoanType().getMonthlyInterestRate(), request.getRequestedTerm());


        return null;
    }


    public boolean checkDTI(Long customerId, BigDecimal monthlyIncome,
                            BigDecimal newLoanAmount, Integer installmentCount) {

        BigDecimal newMonthlyInstallment = newLoanAmount
                .divide(BigDecimal.valueOf(installmentCount), 2, RoundingMode.HALF_UP);

        BigDecimal maxMonthlyPayment = calculateMaxMonthlyPayment(customerId, newMonthlyInstallment);

        BigDecimal dti = maxMonthlyPayment
                .divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return dti.compareTo(BigDecimal.valueOf(40)) <= 0;
    }

    public BigDecimal calculateMaxMonthlyPayment(
            Long customerId,
            BigDecimal newLoanMonthlyInstallment) {

        // Tüm aktif kredilerin aylık taksitlerini topla
        List<Loan> activeLoans = loanRepository.findByCustomerIdAndStatus(
                customerId,
                LoanStatusType.ACTIVE
        );

        BigDecimal totalExistingInstallments = activeLoans.stream()
                .map(Loan::getMonthlyInstallment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Yeni kredi taksitini ekle
        return totalExistingInstallments.add(newLoanMonthlyInstallment);
    }

    public BigDecimal calculateMonthlyInstallment(BigDecimal creditAmount, BigDecimal monthlyInterestRate, int term) {
        BigDecimal interestRatePowTerm = BigDecimal.ONE.add(monthlyInterestRate).pow(term);
        BigDecimal numerator = creditAmount.multiply(monthlyInterestRate).multiply(interestRatePowTerm);
        BigDecimal denominator = interestRatePowTerm.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
