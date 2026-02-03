package com.yasirakbal.secureloanapi.feature.blacklist.service;

import com.yasirakbal.secureloanapi.feature.blacklist.entity.JwtBlacklist;
import com.yasirakbal.secureloanapi.feature.blacklist.repository.JwtBlacklistRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JwtBlacklistService {
    private JwtBlacklistRepository jwtBlacklistRepository;

    public void createJwtBlacklist(JwtBlacklist jwtBlacklist) {
        jwtBlacklistRepository.save(jwtBlacklist);
    }
}
