package com.example.pos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action; // e.g., LOGIN_SUCCESS, PRODUCT_CREATE, USER_DELETE

    @Column(length = 100)
    private String resource; // e.g., PRODUCT, USER, CATEGORY

    private Long resourceId;

    @Column(length = 2000)
    private String details;

    private String ipAddress;

    private String userAgent;
}
