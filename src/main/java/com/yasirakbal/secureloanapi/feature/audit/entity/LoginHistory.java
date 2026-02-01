package com.yasirakbal.secureloanapi.feature.audit.entity;

import com.yasirakbal.secureloanapi.common.entity.BaseEntity;
import com.yasirakbal.secureloanapi.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_histories", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_success", columnList = "success")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 50)
    private String device;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    private Boolean success;

    @Column(length = 255)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime timeStamp;
}
