package com.yasirakbal.secureloanapi.feature.auth.service;

import com.yasirakbal.secureloanapi.common.exception.BusinessException;
import com.yasirakbal.secureloanapi.feature.auth.adapter.AppUserAdapter;
import com.yasirakbal.secureloanapi.feature.auth.dto.LoginResponse;
import com.yasirakbal.secureloanapi.feature.auth.exception.InvalidCredentialsException;
import com.yasirakbal.secureloanapi.feature.auth.exception.UserAccountDisabledException;
import com.yasirakbal.secureloanapi.feature.auth.exception.UserAccountLockedException;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import com.yasirakbal.secureloanapi.feature.user.enums.UserRole;
import com.yasirakbal.secureloanapi.feature.user.exception.EmailDuplicationException;
import com.yasirakbal.secureloanapi.feature.user.exception.IdentityNumberDuplicationException;
import com.yasirakbal.secureloanapi.feature.user.exception.UserCreationValidationException;
import com.yasirakbal.secureloanapi.feature.user.exception.UsernameDuplicationException;
import com.yasirakbal.secureloanapi.feature.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountLockedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtEncoder jwtEncoder;

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

    public LoginResponse login(String username, String password) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getAccountLocked()) {
            if (user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())) {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            } else {
                throw new UserAccountLockedException(user.getLockedUntil());
            }
        }

        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    password
            );

            var authentication = authenticationManager.authenticate(authToken);

            String token = generateToken(authentication);

            LoginResponse.LoginUserResponse userResponse = LoginResponse.LoginUserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().getValue())
                    .fullName(user.getFullName())
                    .username(user.getUsername())
                    .build();

            return LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(1800)
                    .user(userResponse)
                    .build();
        } catch (LockedException e) {
            throw new UserAccountLockedException("Account is temporarily locked");
        } catch (DisabledException e) {
            throw new UserAccountDisabledException();
        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException();
        }
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= 5) {
            user.setAccountLocked(true);
            user.setLockedUntil(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            throw new UserAccountLockedException(user.getLockedUntil())
                    .addDetail("reason", "Too many failed login attempts")
                    .addDetail("maxAttempts", 5);
        }

        userRepository.save(user);

        throw new InvalidCredentialsException()
                .addDetail("remainingAttempts", 5 - attempts);
    }

    private String generateToken(Authentication authentication) {
        var authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        var claimsSet = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuer("secureloan-api")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .claim("scope", authorities)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }
}
