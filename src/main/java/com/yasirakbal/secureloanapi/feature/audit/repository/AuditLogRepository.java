package com.yasirakbal.secureloanapi.feature.audit.repository;

import com.yasirakbal.secureloanapi.feature.audit.entity.SecurityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<SecurityAuditLog, Long>, JpaSpecificationExecutor<SecurityAuditLog> {
}
