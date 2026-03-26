package com.example.pos.controller;

import com.example.pos.dto.*;
import com.example.pos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token management")
public class AuthController {

    private final AuthService authService;
    private final MessageSource messageSource;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with default roles and returns JWT tokens.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request, Locale locale) {
        AuthResponse response = authService.register(request, locale);
        String msg = messageSource.getMessage("auth.register.success", null, locale);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message(msg)
                        .data(response)
                        .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Login existing user", description = "Authenticates a user and returns new JWT access and refresh tokens.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request, Locale locale) {
        AuthResponse response = authService.login(request, locale);
        String msg = messageSource.getMessage("auth.login.success", null, locale);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message(msg)
                .data(response)
                .build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Exchanges a valid refresh token for a new set of access and refresh tokens.")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request, Locale locale) {
        AuthResponse response = authService.refreshToken(request, locale);
        String msg = messageSource.getMessage("auth.refresh.success", null, locale);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message(msg)
                .data(response)
                .build());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the given refresh token.")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request, Locale locale) {
        authService.logout(request, locale);
        String msg = messageSource.getMessage("auth.logout.success", null, locale);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message(msg)
                .build());
    }
}
