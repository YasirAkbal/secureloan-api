package com.yasirakbal.secureloanapi.feature.auth.controller;

import com.yasirakbal.secureloanapi.feature.auth.dto.UserRegisterRequest;
import com.yasirakbal.secureloanapi.feature.auth.dto.UserRegisterResponse;
import com.yasirakbal.secureloanapi.feature.auth.mapper.UserRegisterRequestMapper;
import com.yasirakbal.secureloanapi.feature.auth.mapper.UserRegisterResponseMapper;
import com.yasirakbal.secureloanapi.feature.auth.service.AuthService;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(path="/api/auth")
@AllArgsConstructor
@Validated
public class AuthController {
    private AuthService authService;
    private UserRegisterRequestMapper userRegisterRequestMapper;
    private UserRegisterResponseMapper userRegisterResponseMapper;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        User user = userRegisterRequestMapper.map(request);
        User registeredUser = authService.registerUser(user);

        UserRegisterResponse response = userRegisterResponseMapper.map(registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
