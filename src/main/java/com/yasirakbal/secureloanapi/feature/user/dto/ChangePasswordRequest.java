package com.yasirakbal.secureloanapi.feature.user.dto;

import com.yasirakbal.secureloanapi.feature.auth.annotation.interfaces.PasswordMatches;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@PasswordMatches
public class ChangePasswordRequest {
    @NotBlank
    private String password;

    @NotBlank
    private String matchingPassword;
}
