package com.yasirakbal.secureloanapi.feature.auth.dto;

import com.yasirakbal.secureloanapi.feature.auth.annotation.interfaces.PasswordMatches;
import com.yasirakbal.secureloanapi.feature.auth.annotation.interfaces.ValidEmail;
import com.yasirakbal.secureloanapi.feature.auth.annotation.interfaces.ValidPassword;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@PasswordMatches
public class UserRegisterRequest {
    @NotBlank
    @Min(3)
    @Max(50)
    private String username;

    @NotBlank
    @ValidPassword
    private String password;

    @NotBlank
    private String matchingPassword;

    @NotBlank
    @ValidEmail
    private String email;

    @NotBlank
    private String fullName;

    @NotBlank
    @Size(min = 11, max = 11)
    private String identityNumber;

    @NotBlank
    private String phoneNumber;

    @NonNull
    private LocalDate dateOfBirth;

    @NonNull
    @Positive
    private BigDecimal monthlyIncome;

    @Min(300)
    @Max(850)
    private Integer creditScore;
}
