package com.example.pos.controller;

import com.example.pos.dto.ApiResponse;
import com.example.pos.dto.AssignRolesRequest;
import com.example.pos.dto.CreateUserRequest;
import com.example.pos.dto.UpdateAccessRequest;
import com.example.pos.dto.UserDto;
import com.example.pos.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.<List<UserDto>>builder()
                .success(true)
                .data(userService.getAllUsers())
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequest request, Locale locale) {
        UserDto created = userService.createUser(request, locale);
        String msg = messageSource.getMessage("admin.user.created", null, locale);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message(msg)
                .data(created)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id, Locale locale) {
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .data(userService.getUserById(id, locale))
                .build());
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserDto>> assignRoles(
            @PathVariable Long id,
            @Valid @RequestBody AssignRolesRequest request,
            Locale locale) {
        UserDto updated = userService.assignRoles(id, request, locale);
        String msg = messageSource.getMessage("admin.roles.updated", null, locale);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message(msg)
                .data(updated)
                .build());
    }

    @PutMapping("/{id}/access")
    public ResponseEntity<ApiResponse<UserDto>> updateAccess(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccessRequest request,
            Locale locale) {
        UserDto updated = userService.updateAccess(id, request, locale);
        String msg = messageSource.getMessage("admin.access.updated", null, locale);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message(msg)
                .data(updated)
                .build());
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<UserDto>> setEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            Locale locale) {
        UserDto updated = userService.setEnabled(id, enabled, locale);
        String msg = messageSource.getMessage("admin.user.status.updated", null, locale);
        return ResponseEntity.ok(ApiResponse.<UserDto>builder()
                .success(true)
                .message(msg)
                .data(updated)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id, Locale locale) {
        userService.deleteUser(id, locale);
        String msg = messageSource.getMessage("admin.user.deleted", null, locale);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message(msg)
                .build());
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<String>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .success(true)
                .data(userService.getAllRoles())
                .build());
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<String>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .success(true)
                .data(userService.getAllPermissions())
                .build());
    }
}
