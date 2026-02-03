package com.yasirakbal.secureloanapi.feature.blacklist.repository;

import com.yasirakbal.secureloanapi.feature.blacklist.entity.JwtBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtBlacklistRepository extends JpaRepository<JwtBlacklist, Long> {
}
