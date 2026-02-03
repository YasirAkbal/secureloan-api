package com.yasirakbal.secureloanapi.feature.auth.service;

import com.yasirakbal.secureloanapi.feature.auth.adapter.AppUserAdapter;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import com.yasirakbal.secureloanapi.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        return new AppUserAdapter(user);
    }
}
