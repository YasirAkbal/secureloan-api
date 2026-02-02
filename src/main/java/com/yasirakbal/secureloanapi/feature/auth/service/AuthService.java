package com.yasirakbal.secureloanapi.feature.auth.service;

import com.yasirakbal.secureloanapi.common.exception.BusinessException;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import com.yasirakbal.secureloanapi.feature.user.enums.UserRole;
import com.yasirakbal.secureloanapi.feature.user.exception.EmailDuplicationException;
import com.yasirakbal.secureloanapi.feature.user.exception.IdentityNumberDuplicationException;
import com.yasirakbal.secureloanapi.feature.user.exception.UserCreationValidationException;
import com.yasirakbal.secureloanapi.feature.user.exception.UsernameDuplicationException;
import com.yasirakbal.secureloanapi.feature.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AuthService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(User userToCreate) {
        List<BusinessException> validationErrors = validateUser(userToCreate);
        if(!validationErrors.isEmpty()) {
            throw new UserCreationValidationException(validationErrors, "User registration failed.", HttpStatus.CONFLICT);
        }

        String hashedPassword = passwordEncoder.encode(userToCreate.getPassword());
        userToCreate.setPassword(hashedPassword);
        userToCreate.setRole(UserRole.CUSTOMER);

        return userRepository.save(userToCreate);
    }

    private List<BusinessException> validateUser(User userToCreate) {
        List<BusinessException> validationErrors = new ArrayList<>();

        if(userRepository.existsUserByEmail(userToCreate.getEmail())) {
            validationErrors.add(new EmailDuplicationException(userToCreate.getEmail()));
        }
        if(userRepository.existsUserByUsername(userToCreate.getUsername())) {
            validationErrors.add(new UsernameDuplicationException(userToCreate.getUsername()));
        }
        if(userRepository.existsUserByIdentityNumber(userToCreate.getIdentityNumber())) {
            validationErrors.add(new IdentityNumberDuplicationException(userToCreate.getIdentityNumber()));
        }

        return validationErrors;
    }
}
