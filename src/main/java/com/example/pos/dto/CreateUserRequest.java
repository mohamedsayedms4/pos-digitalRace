package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
@Schema(description = "Request object for creating a new user by an admin")
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "User email address", example = "john@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    @Schema(description = "Initial user password", example = "securePass1")
    private String password;

    @Schema(description = "Whether the user account is enabled upon creation", example = "true")
    private boolean enabled;

    @Schema(description = "Roles to assign to the user (e.g., ROLE_USER, ROLE_ADMIN)", example = "[\"ROLE_USER\"]")
    private Set<String> roles;

    @Schema(description = "Specific permissions to assign (optional)", example = "[]")
    private Set<String> permissions;
}
