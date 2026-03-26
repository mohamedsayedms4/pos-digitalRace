package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Request object for assigning roles")
public class AssignRolesRequest {

    @NotEmpty(message = "Roles list cannot be empty")
    @Schema(description = "List of role names to assign", example = "[\"ROLE_USER\"]")
    private Set<String> roles;
}
