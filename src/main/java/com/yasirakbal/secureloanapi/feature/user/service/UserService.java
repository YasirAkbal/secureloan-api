package com.yasirakbal.secureloanapi.feature.user.service;

import com.yasirakbal.secureloanapi.feature.user.entity.User;
import com.yasirakbal.secureloanapi.feature.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;

    public User getUserByUsername(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return user;
    }
}
