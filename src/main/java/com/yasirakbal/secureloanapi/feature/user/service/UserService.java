package com.yasirakbal.secureloanapi.feature.user.service;

import com.yasirakbal.secureloanapi.feature.blacklist.entity.JwtBlacklist;
import com.yasirakbal.secureloanapi.feature.blacklist.enums.JwtBlacklistReason;
import com.yasirakbal.secureloanapi.feature.blacklist.service.JwtBlacklistService;
import com.yasirakbal.secureloanapi.feature.user.dto.ChangePasswordRequest;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import com.yasirakbal.secureloanapi.feature.user.exception.UserNotFoundException;
import com.yasirakbal.secureloanapi.feature.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtBlacklistService jwtBlacklistService;

    public User getUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return user;
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setPasswordExpired(false);

        String token = jwt.getTokenValue();
        LocalDateTime expiresAt = LocalDateTime.ofInstant(jwt.getExpiresAt(), ZoneOffset.UTC);

        JwtBlacklist jwtBlacklist = JwtBlacklist.builder()
                .userId(userId)
                .reason(JwtBlacklistReason.PASSWORD_CHANGED)
                .blacklistedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .token(token)
                .build();

        jwtBlacklistService.createJwtBlacklist(jwtBlacklist);

        userRepository.save(user);
    }
}
