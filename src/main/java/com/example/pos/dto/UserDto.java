package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User Data Transfer Object")
public class UserDto {
    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "Full name of the user", example = "Admin User")
    private String name;
    
    @Schema(description = "User email address", example = "admin@example.com")
    private String email;
    
    @Schema(description = "Indicates whether the account is active", example = "true")
    private boolean enabled;
    
    @Schema(description = "Set of roles assigned to the user", example = "[\"ROLE_ADMIN\", \"ROLE_USER\"]")
    private Set<String> roles;
    
    @Schema(description = "Specific permission overrides for the user", example = "[\"CREATE_PRODUCT\"]")
    private Set<String> permissions;
}
