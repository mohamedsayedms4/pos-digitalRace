package com.example.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT tokens and user info")
public class AuthResponse {
    @Schema(description = "JWT access token")
    private String accessToken;
    @Schema(description = "JWT refresh token")
    private String refreshToken;
    @Builder.Default
    @Schema(description = "Type of the token", example = "Bearer")
    private String tokenType = "Bearer";
    @Schema(description = "Basic user details")
    private UserDto user;
}
