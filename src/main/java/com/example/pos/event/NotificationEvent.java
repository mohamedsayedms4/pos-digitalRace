package com.example.pos.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationEvent {
    private String title;
    private String message;
    private String type; // e.g., INFO, WARNING, SECURITY
    private Long targetUserId; // null if for roles
    private Set<String> targetRoles; // null if for specific user
    private String actorUsername; // The person who triggered the notification
    private String actionUrl; // optional link
}
