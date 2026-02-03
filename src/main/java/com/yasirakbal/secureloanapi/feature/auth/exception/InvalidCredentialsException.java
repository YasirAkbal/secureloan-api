package com.yasirakbal.secureloanapi.feature.auth.exception;

import com.yasirakbal.secureloanapi.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super("Invalid credentials.", HttpStatus.BAD_REQUEST);
    }
}
