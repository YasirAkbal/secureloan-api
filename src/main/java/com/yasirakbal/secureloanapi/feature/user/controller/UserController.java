package com.yasirakbal.secureloanapi.feature.user.controller;

import com.yasirakbal.secureloanapi.feature.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {
    private UserService userService;
}
