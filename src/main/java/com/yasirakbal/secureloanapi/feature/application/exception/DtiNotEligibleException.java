package com.yasirakbal.secureloanapi.feature.application.exception;

import com.yasirakbal.secureloanapi.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DtiNotEligibleException extends BusinessException {
    public DtiNotEligibleException() {
        super("The calculated dti must be smaller than 0.4", HttpStatus.BAD_REQUEST);
    }
}
