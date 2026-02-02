package com.yasirakbal.secureloanapi.feature.audit.entity;

import com.yasirakbal.secureloanapi.common.entity.BaseEntity;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "security_audit_logs", indexes = {
        @Index(name = "idx_security_audit_logs_user_id", columnList = "user_id"),
        @Index(name = "idx_security_audit_logs_action", columnList = "action"),
        @Index(name = "idx_security_audit_logs_timestamp", columnList = "timestamp"),
        @Index(name = "idx_security_audit_logs_success", columnList = "success")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAuditLog extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 255)
    private String resource;

    @Column(length = 10)
    private String httpMethod;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false)
    private Boolean success;

    @Column(length = 255)
    private String failureReason;

    @Column
    private String details;

    @Column(nullable = false)
    private LocalDateTime timestamp;

}
