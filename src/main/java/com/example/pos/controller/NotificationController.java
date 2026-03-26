package com.example.pos.controller;

import com.example.pos.dto.ApiResponse;
import com.example.pos.entity.Notification;
import com.example.pos.entity.User;
import com.example.pos.repository.NotificationRepository;
import com.example.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for user-specific real-time alerts and history")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get my notifications", description = "Retrieves all stored notifications for the currently authenticated user.")
    public ResponseEntity<ApiResponse<List<Notification>>> getMyNotifications(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(ApiResponse.<List<Notification>>builder()
                .success(true)
                .data(notificationRepository.findByUserIdOrderByTimestampDesc(user.getId()))
                .build());
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read by the authenticated user.")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id, Authentication auth) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!notification.getUserId().equals(user.getId())) {
             return ResponseEntity.status(403).build();
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).build());
    }
}
