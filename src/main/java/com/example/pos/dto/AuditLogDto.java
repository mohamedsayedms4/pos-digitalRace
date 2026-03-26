package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "Audit Log Data Transfer Object")
public class AuditLogDto {
    @Schema(description = "Log entry ID")
    private Long id;
    
    @Schema(description = "Time the action occurred")
    private LocalDateTime timestamp;
    
    @Schema(description = "Email of the user who performed the action", example = "admin@example.com")
    private String username;
    
    @Schema(description = "Action performed", example = "PRODUCT_CREATE")
    private String action;
    
    @Schema(description = "Resource category affected", example = "PRODUCT")
    private String resource;
    
    @Schema(description = "ID of the specific resource affected", example = "10")
    private Long resourceId;
    
    @Schema(description = "Detailed log message", example = "Product 'Mouse' was created.")
    private String details;
    
    @Schema(description = "IP address of the client", example = "192.168.1.1")
    private String ipAddress;
    
    @Schema(description = "User agent of the client", example = "Mozilla/5.0...")
    private String userAgent;
}
