package com.yasirakbal.secureloanapi.feature.officer.controller;

import com.yasirakbal.secureloanapi.feature.application.entity.LoanApplication;
import com.yasirakbal.secureloanapi.feature.application.enums.LoanApplicationStatus;
import com.yasirakbal.secureloanapi.feature.application.service.LoanApplicationService;
import com.yasirakbal.secureloanapi.feature.loan.entity.Loan;
import com.yasirakbal.secureloanapi.feature.officer.dto.ApproveLoanApplicationResponse;
import com.yasirakbal.secureloanapi.feature.officer.dto.GetLoanApplicationsPaginationResponse;
import com.yasirakbal.secureloanapi.feature.officer.dto.GetLoanApplicationsResponse;
import com.yasirakbal.secureloanapi.feature.officer.mapper.ApproveLoanAppResponseMapper;
import com.yasirakbal.secureloanapi.feature.officer.mapper.GetLoanApplicationsResponseMapper;
import com.yasirakbal.secureloanapi.feature.officer.service.CreditOfficerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/api/officer")
@AllArgsConstructor
@Validated
public class CreditOfficerController {
    private final CreditOfficerService creditOfficerService;
    private LoanApplicationService loanApplicationService;
    private GetLoanApplicationsResponseMapper getLoanApplicationsResponseMapper;
    private ApproveLoanAppResponseMapper approveLoanAppResponseMapper;

    @GetMapping("/applications")
    public ResponseEntity<GetLoanApplicationsPaginationResponse> getApplications(
            @RequestParam(required = false) LoanApplicationStatus status,

            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Page<LoanApplication> loanApplicationPage = loanApplicationService.getAllLoanApplications(status, page, size, sortBy, direction);

        List<GetLoanApplicationsResponse> responses = loanApplicationPage.stream()
                .map(l -> getLoanApplicationsResponseMapper.map(l)).toList();
        GetLoanApplicationsPaginationResponse paginationResponse = new GetLoanApplicationsPaginationResponse(loanApplicationPage, responses);

        return ResponseEntity.ok(paginationResponse);
    }

    @GetMapping("/applications/{id}/approve")
    public ResponseEntity<ApproveLoanApplicationResponse> approveLoanApplication(@PathVariable @Positive Long id
    ) {
        Loan createdLoan = creditOfficerService.approveLoanApplication(id);
        ApproveLoanApplicationResponse response = approveLoanAppResponseMapper.map(createdLoan);

        return ResponseEntity.ok(response);
    }
}
