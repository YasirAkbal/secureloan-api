package com.yasirakbal.secureloanapi.feature.application.service;

import com.yasirakbal.secureloanapi.common.exception.BusinessException;
import com.yasirakbal.secureloanapi.common.exception.ResourceNotFoundException;
import com.yasirakbal.secureloanapi.common.utils.DateUtils;
import com.yasirakbal.secureloanapi.feature.application.dto.CreateLoanApplicationRequest;
import com.yasirakbal.secureloanapi.feature.application.entity.LoanApplication;
import com.yasirakbal.secureloanapi.feature.application.enums.LoanApplicationStatus;
import com.yasirakbal.secureloanapi.feature.application.exception.CustomerAgeRequestedTermNotEligibleException;
import com.yasirakbal.secureloanapi.feature.application.exception.DtiNotEligibleException;
import com.yasirakbal.secureloanapi.feature.application.exception.LoanApplicationCannotBeDeletedException;
import com.yasirakbal.secureloanapi.feature.application.exception.MonthlyIncomeNotEnoughException;
import com.yasirakbal.secureloanapi.feature.application.repository.LoanApplicationRepository;
import com.yasirakbal.secureloanapi.feature.loan.entity.Loan;
import com.yasirakbal.secureloanapi.feature.loan.enums.LoanStatusType;
import com.yasirakbal.secureloanapi.feature.loan.enums.LoanType;
import com.yasirakbal.secureloanapi.feature.loan.repository.LoanRepository;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import com.yasirakbal.secureloanapi.feature.user.service.UserService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class LoanApplicationService {
    private LoanApplicationRepository loanApplicationRepository;
    private LoanRepository loanRepository;
    private UserService userService;
    private EntityManager entityManager;

    public LoanApplication createApplication(CreateLoanApplicationRequest request, Long customerId) {
        User customer = userService.getUserById(customerId);
        LoanType requestedLoanType = request.getLoanType();
        BigDecimal requestedAmount = request.getRequestedAmount();
        String purpose = request.getPurpose();
        int requestedTerm = request.getRequestedTerm();
        BigDecimal interestRate = requestedLoanType.getMonthlyInterestRate();

        List<BusinessException> creationErrors = new ArrayList<>();

        if(!requestedLoanType.checkIfItsEligible(customer.getMonthlyIncome())) {
            creationErrors.add(new MonthlyIncomeNotEnoughException());
        }

        LocalDate dateWhenCreditWillBeFinished = LocalDate.now().plusMonths(requestedTerm);
        int customerAgeAfterCreditFinished = Period.between(customer.getBirthDate(), dateWhenCreditWillBeFinished).getYears();
        if(customerAgeAfterCreditFinished > 65) {
            creationErrors.add(new CustomerAgeRequestedTermNotEligibleException(65, customerAgeAfterCreditFinished));
        }

        BigDecimal dti = getDti(customerId, customer.getMonthlyIncome(),
                requestedAmount, requestedTerm);
        if(dti.compareTo(BigDecimal.valueOf(40)) > 0)
        {
            creationErrors.add(new DtiNotEligibleException(BigDecimal.valueOf(40), dti));
        }

        BigDecimal monthlyInstallment = calculateMonthlyInstallment(requestedAmount,
                request.getLoanType().getMonthlyInterestRate(), request.getRequestedTerm());
        BigDecimal totalPayment = monthlyInstallment.multiply(BigDecimal.valueOf(requestedTerm));

        if(!creationErrors.isEmpty()) { //rejection
            var rejectedLoanApp = buildForRejectedCase(customerId, requestedLoanType, requestedAmount,
                    requestedTerm, purpose, monthlyInstallment, interestRate, totalPayment, dti, creationErrors);

            loanApplicationRepository.save(rejectedLoanApp);
            return rejectedLoanApp;
        }


        var pendingLoanApp = buildForPendingCase(customerId, requestedLoanType, requestedAmount,
                requestedTerm, purpose, monthlyInstallment, interestRate, totalPayment, dti);
        loanApplicationRepository.save(pendingLoanApp);

        return pendingLoanApp;
    }

    private LoanApplication buildForRejectedCase(Long customerId, LoanType requestedLoanType, BigDecimal requestedAmount,
                                                 int requestedTerm, String purpose, BigDecimal monthlyInstallment,
                                                 BigDecimal interestRate, BigDecimal totalPayment, BigDecimal dti, List<BusinessException> creationErrors) {
        return build(customerId, requestedLoanType, requestedAmount, requestedTerm, purpose, monthlyInstallment, interestRate, totalPayment, dti, creationErrors, LoanApplicationStatus.AUTO_REJECTED);
    }

    private LoanApplication buildForPendingCase(Long customerId, LoanType requestedLoanType, BigDecimal requestedAmount, int requestedTerm, String purpose, BigDecimal monthlyInstallment, BigDecimal interestRate, BigDecimal totalPayment, BigDecimal dti) {
        return build(customerId, requestedLoanType, requestedAmount, requestedTerm, purpose, monthlyInstallment, interestRate, totalPayment, dti, null, LoanApplicationStatus.PENDING);
    }

    private LoanApplication build(Long customerId, LoanType requestedLoanType, BigDecimal requestedAmount, int requestedTerm,
                                  String purpose, BigDecimal monthlyInstallment, BigDecimal interestRate,
                                  BigDecimal totalPayment, BigDecimal dti, List<BusinessException> creationErrors, LoanApplicationStatus status) {
        User customerRef = entityManager.getReference(User.class, customerId);

        var rejectedLoanApp = LoanApplication.builder()
                .customer(customerRef)
                .loanType(requestedLoanType)
                .requestedAmount(requestedAmount)
                .requestedTerm(requestedTerm)
                .purpose(purpose)
                .monthlyInstallment(monthlyInstallment)
                .interestRate(interestRate)
                .totalPayment(totalPayment)
                .dtiRatio(dti)
                .status(status)
                .evaluatedAt(LocalDateTime.now())
                .rejectionReasons(creationErrors == null ? null : String.join("; ", creationErrors.stream().map(Throwable::getMessage).toList()))
                .build();
        return rejectedLoanApp;
    }


    public BigDecimal getDti(Long customerId, BigDecimal monthlyIncome,
                            BigDecimal newLoanAmount, Integer installmentCount) {

        BigDecimal newMonthlyInstallment = newLoanAmount
                .divide(BigDecimal.valueOf(installmentCount), 2, RoundingMode.HALF_UP);

        BigDecimal maxMonthlyPayment = calculateMaxMonthlyPayment(customerId, newMonthlyInstallment);

        BigDecimal dti = maxMonthlyPayment
                .divide(monthlyIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return dti;
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

    public List<LoanApplication> getCustomersApplications(Long customerId, LoanApplicationStatus status) {
        return status == null
                ? loanApplicationRepository.findByCustomerId(customerId)
                : loanApplicationRepository.findByCustomerIdAndStatus(customerId, status);
    }

    public LoanApplication getLoanApplicationById(Long id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", id));
    }

    @Transactional
    public void deleteLoanApplication(Long id) {
        LoanApplication loanApplication = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoanApplication", id));

        if(!loanApplication.getStatus().equals(LoanApplicationStatus.PENDING)) {
            throw new LoanApplicationCannotBeDeletedException();
        }

        loanApplication.setStatus(LoanApplicationStatus.CANCELLED);
        loanApplicationRepository.save(loanApplication);
    }
}
