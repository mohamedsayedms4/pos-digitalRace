package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Set;

@Data
@Schema(description = "Request object for updating a user's roles and permissions")
public class UpdateAccessRequest {
    @Schema(description = "New set of roles", example = "[\"ROLE_USER\", \"ROLE_MANAGER\"]")
    private Set<String> roles;
    
    @Schema(description = "New set of specific permissions", example = "[\"DELETE_PRODUCT\"]")
    private Set<String> permissions;
}
