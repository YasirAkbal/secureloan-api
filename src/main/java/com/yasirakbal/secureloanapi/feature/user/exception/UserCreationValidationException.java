package com.yasirakbal.secureloanapi.feature.user.exception;

import com.yasirakbal.secureloanapi.common.exception.BusinessException;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class UserCreationValidationException extends BusinessException {
    @Getter
    private final List<BusinessException> creationErrors;

    public UserCreationValidationException(List<BusinessException> creationErrors) {
        this(creationErrors, "User creation failed.", HttpStatus.CONFLICT);
    }

    public UserCreationValidationException(List<BusinessException> creationErrors, String message, HttpStatus statusCode) {
        super(message, statusCode);
        this.creationErrors = creationErrors;
        addDetail("errorCount", creationErrors.size());
        addDetail("items", creationErrors);
    }

    @Data
    @Builder
    public static class OrderItemError {
        private String errorCode;
        private String errorMessage;
        private Map<String, Object> additionalInfo;
    }
}
