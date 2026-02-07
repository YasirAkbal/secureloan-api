package com.yasirakbal.secureloanapi.feature.admin.service;

import com.yasirakbal.secureloanapi.feature.user.entity.User;
import com.yasirakbal.secureloanapi.feature.user.exception.UserNotFoundException;
import com.yasirakbal.secureloanapi.feature.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminService {
    private UserRepository userRepository;

    @Transactional
    public void lockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setAccountLocked(true);
        user.setLockedUntil(null);
        userRepository.save(user);

        //audit log
    }

    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        userRepository.save(user);
    }

    @Transactional
    public void forceLogoutUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setTokensInvalidatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

}
