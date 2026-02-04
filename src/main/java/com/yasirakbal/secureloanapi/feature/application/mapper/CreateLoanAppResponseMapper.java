package com.yasirakbal.secureloanapi.feature.application.mapper;

import com.yasirakbal.secureloanapi.common.mapper.BaseMapper;
import com.yasirakbal.secureloanapi.feature.application.dto.CreateLoanApplicationResponse;
import com.yasirakbal.secureloanapi.feature.loan.entity.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreateLoanAppResponseMapper extends BaseMapper<Loan, CreateLoanApplicationResponse> {
}
