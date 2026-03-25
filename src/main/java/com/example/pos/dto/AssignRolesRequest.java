package com.example.pos.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AssignRolesRequest {

    @NotEmpty(message = "Roles list cannot be empty")
    private Set<String> roles;
}
