package com.yasirakbal.secureloanapi.feature.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetAuditLogsResponse {
    private Long id;
    private GetAuditLogUserView user;
    private String action;
    private String resource;
    private String ipAddress;
    private boolean success;
    private LocalDateTime timestamp;
}
