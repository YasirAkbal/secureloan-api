package com.yasirakbal.secureloanapi.feature.auth.exception;

import com.yasirakbal.secureloanapi.common.exception.NonRollbackBusinessException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends NonRollbackBusinessException {
    public InvalidCredentialsException() {
        super("Invalid credentials.", HttpStatus.BAD_REQUEST);
    }
}
