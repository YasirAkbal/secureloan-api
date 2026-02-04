package com.yasirakbal.secureloanapi.feature.application.controller;

import com.yasirakbal.secureloanapi.feature.application.dto.CreateLoanApplicationRequest;
import com.yasirakbal.secureloanapi.feature.application.dto.CreateLoanApplicationResponse;
import com.yasirakbal.secureloanapi.feature.application.mapper.CreateLoanAppResponseMapper;
import com.yasirakbal.secureloanapi.feature.application.service.LoanApplicationService;
import com.yasirakbal.secureloanapi.feature.loan.entity.Loan;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="/api/applications")
@AllArgsConstructor
@Validated
public class LoanApplicationController {
    private LoanApplicationService loanApplicationService;
    private CreateLoanAppResponseMapper createLoanAppResponseMapper;

    @PostMapping
    public ResponseEntity<CreateLoanApplicationResponse> createLoanApplication(@RequestBody @Valid CreateLoanApplicationRequest request,
                                                                               @AuthenticationPrincipal Jwt jwt) {
        Loan createdLoan = loanApplicationService.createApplication(request, jwt.getClaim("userId"));

        CreateLoanApplicationResponse response = createLoanAppResponseMapper.map(createdLoan);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
