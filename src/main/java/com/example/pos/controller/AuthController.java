package com.example.pos.controller;

import com.example.pos.dto.*;
import com.example.pos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MessageSource messageSource;

    @PostMapping("/register")
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
