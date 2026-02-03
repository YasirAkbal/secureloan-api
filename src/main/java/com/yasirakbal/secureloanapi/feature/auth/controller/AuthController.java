package com.yasirakbal.secureloanapi.feature.auth.controller;

import com.yasirakbal.secureloanapi.feature.auth.adapter.AppUserAdapter;
import com.yasirakbal.secureloanapi.feature.auth.dto.LoginRequest;
import com.yasirakbal.secureloanapi.feature.auth.dto.LoginResponse;
import com.yasirakbal.secureloanapi.feature.auth.dto.UserRegisterRequest;
import com.yasirakbal.secureloanapi.feature.auth.dto.UserRegisterResponse;
import com.yasirakbal.secureloanapi.feature.auth.mapper.UserRegisterRequestMapper;
import com.yasirakbal.secureloanapi.feature.auth.mapper.UserRegisterResponseMapper;
import com.yasirakbal.secureloanapi.feature.auth.service.AuthService;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;


@RestController
@RequestMapping(path="/api/auth")
@AllArgsConstructor
@Validated
public class AuthController {
    private AuthService authService;
    private UserRegisterRequestMapper userRegisterRequestMapper;
    private UserRegisterResponseMapper userRegisterResponseMapper;

    private JwtEncoder jwtEncoder;
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        User user = userRegisterRequestMapper.map(request);
        User registeredUser = authService.registerUser(user);

        UserRegisterResponse response = userRegisterResponseMapper.map(registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/hello")
    public String hello(Authentication authentication) {
        return "Hello, %s. Your authorities are: %s".formatted(authentication.getName(), authentication.getAuthorities());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        );

        var authentication = authenticationManager.authenticate(authToken);

        String token = generateToken(authentication);

        AppUserAdapter adapter = (AppUserAdapter) authentication.getPrincipal();
        User user = adapter.getUser();

        LoginResponse.LoginUserResponse userResponse = LoginResponse.LoginUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().getValue())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .build();

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(1800)
                .user(userResponse)
                .build());
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
