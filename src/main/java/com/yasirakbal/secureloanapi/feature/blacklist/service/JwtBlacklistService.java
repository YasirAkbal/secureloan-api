package com.yasirakbal.secureloanapi.feature.blacklist.service;

import com.yasirakbal.secureloanapi.feature.audit.annotation.Auditable;
import com.yasirakbal.secureloanapi.feature.audit.enums.AuditEventType;
import com.yasirakbal.secureloanapi.feature.blacklist.entity.JwtBlacklist;
import com.yasirakbal.secureloanapi.feature.blacklist.repository.JwtBlacklistRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class JwtBlacklistService {
    private JwtBlacklistRepository jwtBlacklistRepository;

    @Transactional
    @Auditable(eventType = AuditEventType.TOKEN_BLACKLISTED)
    public void createJwtBlacklist(JwtBlacklist jwtBlacklist) {
        jwtBlacklistRepository.save(jwtBlacklist);
    }

    public boolean isBlacklisted(String jwtToken) {
        return jwtBlacklistRepository.existsJwtBlacklistByToken(jwtToken);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = jwtBlacklistRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("🗑️ Cleaned up {} expired tokens from blacklist", deleted);
        }
    }
}
